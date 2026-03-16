package com;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class FCFSController {

	@FXML
	private Spinner<Integer> burstSpinner;

	@FXML
	private ListView<Proceso> processListView;

	@FXML
	private TableView<Proceso> processPlanifierTableView;

	@FXML
	private TableColumn<Proceso, String> processColumn;

	@FXML
	private TableColumn<Proceso, Number> burstColumn;

	@FXML
	private TableColumn<Proceso, Number> remainingColumn;

	@FXML
	private TableColumn<Proceso, String> stateColumn;

	@FXML
	private TableColumn<Proceso, String> waitColumn;

	@FXML
	private Button createButton;

	@FXML
	private Button modifyButton;

	@FXML
	private Button deleteButton;

	@FXML
	private Button startButton;

	@FXML
	private Button stopResumeButton;

	@FXML
	private Button restartButton;

	@FXML
	private Text processText;

	@FXML
	private Text resultText;

	private final ObservableList<Proceso> procesos = FXCollections.observableArrayList();
	private Timeline simulationTimeline;
	private Proceso procesoActivo;
	private boolean simulationFinished;
	private int clockSecond;
	private int currentProcessIndex;

	@FXML
	private void initialize() {
		burstSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));

		processColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNombreProceso()));
		burstColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getTiempoRafaga()));
		remainingColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getTiempoRestante()));
		stateColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEstado()));
		waitColumn.setCellValueFactory(cell -> {
			int espera = cell.getValue().getTiempoEspera();
			return new SimpleStringProperty(espera >= 0 ? String.valueOf(espera) : "-");
		});

		processListView.setItems(procesos);
		processPlanifierTableView.setItems(procesos);

		processListView.setCellFactory(listView -> new ListCell<>() {
			@Override
			protected void updateItem(Proceso item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
				} else {
					setText(item.getNombreProceso() + ", rafaga " + item.getTiempoRafaga());
				}
			}
		});

		processPlanifierTableView.setRowFactory(table -> new TableRow<>() {
			@Override
			protected void updateItem(Proceso item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setStyle("");
				} else if (item == procesoActivo) {
					setStyle("-fx-background-color: #9DF59D;");
				} else {
					setStyle("");
				}
			}
		});

		processListView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
			if (simulationIsRunning()) {
				return;
			}
			var node = event.getPickResult().getIntersectedNode();
			while (node != null && !(node instanceof ListCell<?>)) {
				node = node.getParent();
			}
			if (node instanceof ListCell<?>) {
				ListCell<?> clickedCell = (ListCell<?>) node;
				int clickedIndex = clickedCell.getIndex();
				int selectedIndex = processListView.getSelectionModel().getSelectedIndex();
				if (clickedIndex == selectedIndex && selectedIndex >= 0) {
					processListView.getSelectionModel().clearSelection();
					event.consume();
					updateSelectionState();
				}
			}
		});

		processListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> updateSelectionState());
		setInitialState();
	}

	@FXML
	private void create() {
		if (simulationIsRunning()) {
			return;
		}
		int burst = burstSpinner.getValue();
		Proceso nuevo = new Proceso(procesos.size() + 1, burst);
		procesos.add(nuevo);
		refreshViews();
		updateButtonsByContent();
	}

	@FXML
	private void modify() {
		if (simulationIsRunning()) {
			return;
		}
		Proceso seleccionado = processListView.getSelectionModel().getSelectedItem();
		if (seleccionado == null) {
			return;
		}
		int nuevaRafaga = burstSpinner.getValue();
		seleccionado.setTiempoRafaga(nuevaRafaga);
		seleccionado.setTiempoRestante(nuevaRafaga);
		seleccionado.setEstado("LISTO");
		seleccionado.setTiempoEspera(-1);
		resultText.setText("");
		refreshViews();
	}

	@FXML
	private void delete() {
		if (simulationIsRunning()) {
			return;
		}
		Proceso seleccionado = processListView.getSelectionModel().getSelectedItem();
		if (seleccionado == null) {
			return;
		}
		procesos.remove(seleccionado);
		renumberProcesses();
		processListView.getSelectionModel().clearSelection();
		processText.setText("Nuevo Proceso");
		resultText.setText("");
		refreshViews();
		updateButtonsByContent();
	}

	@FXML
	private void start() {
		if (procesos.isEmpty() || simulationIsRunning() || simulationFinished) {
			return;
		}

		resetProcessesForSimulation();
		blockEditionWhileRunning(true);
		stopResumeButton.setDisable(false);
		stopResumeButton.setText("Pausar");
		startButton.setDisable(true);
		currentProcessIndex = 0;
		clockSecond = 1;
		runSimulation();
	}

	@FXML
	private void stopResume() {
		if (simulationTimeline == null || simulationFinished) {
			return;
		}
		if (simulationTimeline.getStatus() == Animation.Status.RUNNING) {
			simulationTimeline.pause();
			stopResumeButton.setText("Reanudar");
		} else {
			simulationTimeline.play();
			stopResumeButton.setText("Pausar");
		}
	}

	@FXML
	private void restart() {
		if (simulationTimeline != null) {
			simulationTimeline.stop();
			simulationTimeline = null;
		}

		for (Proceso proceso : procesos) {
			proceso.setTiempoRestante(proceso.getTiempoRafaga());
			proceso.setEstado("LISTO");
			proceso.setTiempoEspera(-1);
		}

		procesoActivo = null;
		simulationFinished = false;
		clockSecond = 0;
		currentProcessIndex = 0;
		resultText.setText("");
		stopResumeButton.setText("Pausar");

		blockEditionWhileRunning(false);
		processListView.getSelectionModel().clearSelection();
		processText.setText("Nuevo Proceso");
		updateButtonsByContent();
		updateSelectionState();
		refreshViews();
	}

	private void runSimulation() {
		simulationTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> processSimulationTick()));
		simulationTimeline.setCycleCount(Animation.INDEFINITE);
		simulationTimeline.play();
	}

	private void processSimulationTick() {
		if (currentProcessIndex >= procesos.size()) {
			finishSimulation();
			return;
		}

		Proceso actual = procesos.get(currentProcessIndex);
		if (actual.getEstado().equals("Listo") && actual.getTiempoEspera() < 0) {
			actual.setTiempoEspera(clockSecond - 1);
		}

		procesoActivo = actual;
		actual.setEstado("Ejecutando");
		resultText.setText(actual.getNombreProceso() + " se esta resolviendo en el micro segundo " + clockSecond);
		actual.setTiempoRestante(actual.getTiempoRestante() - 1);

		if (actual.getTiempoRestante() <= 0) {
			actual.setTiempoRestante(0);
			actual.setEstado("Terminado");
			currentProcessIndex++;
		}

		clockSecond++;
		refreshViews();
	}

	private void finishSimulation() {
		if (simulationTimeline != null) {
			simulationTimeline.stop();
		}
		simulationFinished = true;
		procesoActivo = null;
		stopResumeButton.setText("Pausar");
		createButton.setDisable(false);
		modifyButton.setDisable(processListView.getSelectionModel().getSelectedItem() == null);
		deleteButton.setDisable(processListView.getSelectionModel().getSelectedItem() == null);
		burstSpinner.setDisable(false);
		processListView.setDisable(false);
		stopResumeButton.setDisable(false);
		startButton.setDisable(true);
		resultText.setText(buildFinalResultText());
		refreshViews();
	}

	private String buildFinalResultText() {
		if (procesos.isEmpty()) {
			return "";
		}

		StringBuilder builder = new StringBuilder();
		double totalEspera = 0;
		for (Proceso proceso : procesos) {
			int espera = Math.max(0, proceso.getTiempoEspera());
			totalEspera += espera;
			builder.append(proceso.getNombreProceso())
				.append(": Tiempo de espera ")
				.append(espera)
				.append("\n");
		}

		double promedio = totalEspera / procesos.size();
		builder.append("Tiempo promedio de espera: ")
			.append(String.format("%.2f", promedio));
		return builder.toString();
	}

	private void resetProcessesForSimulation() {
		for (Proceso proceso : procesos) {
			proceso.setTiempoRestante(proceso.getTiempoRafaga());
			proceso.setEstado("Listo");
			proceso.setTiempoEspera(-1);
		}
		simulationFinished = false;
		resultText.setText("");
		refreshViews();
	}

	private boolean simulationIsRunning() {
		return simulationTimeline != null && simulationTimeline.getStatus() == Animation.Status.RUNNING;
	}

	private void renumberProcesses() {
		for (int i = 0; i < procesos.size(); i++) {
			procesos.get(i).setPid(i + 1);
		}
	}

	private void updateSelectionState() {
		Proceso seleccionado = processListView.getSelectionModel().getSelectedItem();
		if (seleccionado == null) {
			createButton.setDisable(false);
			modifyButton.setDisable(true);
			deleteButton.setDisable(true);
			processText.setText("Nuevo Proceso");
		} else {
			createButton.setDisable(true);
			modifyButton.setDisable(false);
			deleteButton.setDisable(false);
			processText.setText(seleccionado.getNombreProceso());
		}
		if (!simulationFinished) {
			startButton.setDisable(procesos.isEmpty());
		}
		stopResumeButton.setDisable(procesos.isEmpty());
		restartButton.setDisable(procesos.isEmpty());
	}

	private void updateButtonsByContent() {
		if (procesos.isEmpty()) {
			startButton.setDisable(true);
			stopResumeButton.setDisable(true);
			restartButton.setDisable(true);
		} else if (!simulationFinished) {
			startButton.setDisable(false);
			stopResumeButton.setDisable(false);
			restartButton.setDisable(false);
		}
		if (processListView.getSelectionModel().getSelectedItem() == null) {
			createButton.setDisable(false);
			modifyButton.setDisable(true);
			deleteButton.setDisable(true);
		}
	}

	private void setInitialState() {
		processText.setText("Nuevo Proceso");
		resultText.setText("");
		createButton.setDisable(false);
		modifyButton.setDisable(true);
		deleteButton.setDisable(true);
		startButton.setDisable(true);
		stopResumeButton.setDisable(true);
		restartButton.setDisable(true);
	}

	private void blockEditionWhileRunning(boolean blocked) {
		burstSpinner.setDisable(blocked);
		processListView.setDisable(blocked);
		createButton.setDisable(blocked);
		modifyButton.setDisable(blocked);
		deleteButton.setDisable(blocked);
	}

	private void refreshViews() {
		processListView.refresh();
		processPlanifierTableView.refresh();
	}
}
