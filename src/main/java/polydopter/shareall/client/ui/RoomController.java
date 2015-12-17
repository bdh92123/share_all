package polydopter.shareall.client.ui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;
import javafx.util.StringConverter;
import polydopter.shareall.client.net.PacketProcessor;
import polydopter.shareall.client.net.PacketProcessor.FileTransferTask;
import polydopter.shareall.client.net.ProgressHandler;
import polydopter.shareall.client.net.UserHolder;
import polydopter.shareall.client.storage.ClientStorageManager;
import polydopter.shareall.client.ui.base.BaseController;
import polydopter.shareall.client.ui.base.PacketHandler;
import polydopter.shareall.client.ui.dialog.Dialogs;
import polydopter.shareall.client.util.CustomListCell;
import polydopter.shareall.client.util.FXUtil;
import polydopter.shareall.common.Logger;
import polydopter.shareall.common.action.ChatAction;
import polydopter.shareall.common.action.draw.DrawingAction;
import polydopter.shareall.common.action.draw.EllipseAction;
import polydopter.shareall.common.action.draw.FillType;
import polydopter.shareall.common.action.draw.PathAction;
import polydopter.shareall.common.action.draw.RectAction;
import polydopter.shareall.common.action.draw.UndoAction;
import polydopter.shareall.common.action.image.ImageAction;
import polydopter.shareall.common.action.media.MusicAction;
import polydopter.shareall.common.action.media.PauseAction;
import polydopter.shareall.common.action.media.PlayAction;
import polydopter.shareall.common.action.media.SeekAction;
import polydopter.shareall.common.action.media.StopAction;
import polydopter.shareall.common.data.FileKey;
import polydopter.shareall.common.data.FileType;
import polydopter.shareall.common.data.ParamMap;
import polydopter.shareall.common.data.RoomMeta;
import polydopter.shareall.common.data.User;
import polydopter.shareall.common.packet.ErrorCode;
import polydopter.shareall.common.packet.PacketCode;
import polydopter.shareall.common.packet.RequestPacket;
import polydopter.shareall.common.packet.ResponsePacket;
import polydopter.shareall.common.sync.ImageSync;
import polydopter.shareall.common.sync.MusicSync;
import polydopter.shareall.server.data.Room;

public class RoomController extends BaseController implements PacketHandler {

	enum DrawingMode {
		NONE, LINE, PENCIL, BRUSH, RECT, ELLIPSE, TEXT
	}

	@FXML
	Button undoButton, redoButton;
	@FXML
	Button musicAddButton, musicRemoveButton, imageAddButton, imageRemoveButton, fileAddButton, fileRemoveButton;
	@FXML
	Button captureButton;
	@FXML 
	ImageView playButton, nextButton, prevButton, exitButton, messageIcon;
	@FXML
	SplitPane imageSplitPane;
	@FXML
	Slider playSlider;
	@FXML
	TextField messageField;
	@FXML
	TabPane tabPane;
	@FXML
	Tab musicTab, imageTab;
	@FXML
	ProgressBar playingProgressBar;
	@FXML
	ToolBar imageToolbar;
	@FXML
	TextArea chatArea;

	@FXML
	ToggleButton moveButton, pencilButton, brushButton, rectButton, ellipseButton, lineButton;

	@FXML
	Label roomTitle;

	@FXML
	ListView<FileItem> fileListView, musicListView, imageListView;
	@FXML
	ListView<User> userListView;

	@FXML
	Pane canvasPane, colorPane;

	@FXML
	Pane imageListPane;
	
	@FXML
	ScrollPane imagePane;
	
	@FXML
	ImageView imageView;
	
	@FXML
	ScrollPane canvasScrollPane;

	@FXML
	ColorPicker majorColorPicker, minorColorPicker;

	@FXML
	ComboBox<Integer> strokeWidthBox;

	@FXML
	Label playingLabel, playTimeLabel;
	
	@FXML
	private CheckBox fillCheck;

	private DrawingMode drawingMode;
	private Point2D anchorPoint;
	private Path tempPath;
	private Rectangle tempRect;
	private Ellipse tempEllipse;
	private boolean startDrag;
	private MediaPlayer currentPlayer;
	private FileItem currentPlayingItem;
	private Map<FileItem, MediaPlayer> players;
	private Map<User, List<Node>> drawingMap;
	private User master;

	private ImageSync imageSync = new ImageSync();
    private MusicSync musicSync = new MusicSync();
    private Map<User, Boolean> hearFrom = new HashMap<>();
    
	private Room room;
	private Map<FileKey, FileTransferTask> uploadTasks;
	private Map<FileKey, FileTransferTask> downloadTasks;
	private FileItem currentSelectedImage;
	private CaptureController captureController;

	public RoomController(BaseController parent) {
		super(parent, "room.fxml", false);

	}

	protected void initController(boolean initialize) {
		if (initialize) {
			uploadTasks = new ConcurrentHashMap<>();
			downloadTasks = new ConcurrentHashMap<>();
			players = new HashMap<>();
			drawingMap = new HashMap<>();
			playingProgressBar.prefWidthProperty().bind(playSlider.widthProperty());
			Platform.runLater(()->{
				final StackPane region = (StackPane) tabPane.lookup(".headers-region");
		        final StackPane regionTop = (StackPane) tabPane.lookup(".tab-header-area");
		        regionTop.widthProperty().addListener(new ChangeListener<Number>() {

		            @Override
		            public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
		                Insets in = regionTop.getPadding();
		                regionTop.setPadding(new Insets(
		                        in.getTop(),
		                        in.getRight(),
		                        in.getBottom(),
		                        arg2.doubleValue() / 2 - region.getWidth() / 2));
		            }
		        });
		        
		        regionTop.paddingProperty().addListener((observable,oldInsets,newInsets)->{
                	if(newInsets.getLeft() == 0) {
                		Insets in = regionTop.getPadding();
                		regionTop.setPadding(new Insets(
		                        in.getTop(),
		                        in.getRight(),
		                        in.getBottom(),
		                        Math.max(regionTop.getWidth() / 2 - region.getWidth() / 2, 1)));		
                	}
                });
		        getStage().setWidth(getStage().getWidth() + 1);
			});
			
	        playingProgressBar.progressProperty().bind(playSlider.valueProperty().divide(playSlider.maxProperty()));
			canvasPane.setOnMousePressed((event) -> {
				RoomController.this.mousePressed(event);
			});
			canvasPane.setOnMouseDragged((event) -> {
				RoomController.this.mouseDragged(event);
			});
			canvasPane.setOnMouseReleased((event) -> {
				RoomController.this.mouseReleased(event);
			});
						
			SplitPane.setResizableWithParent(imageListPane, Boolean.FALSE);

			Platform.runLater(new Runnable() {
				@Override
				public void run() {

					FXUtil.setCellFactory(fileListView, FileCell.class, RoomController.this);
					FXUtil.setCellFactory(musicListView, FileCell.class, RoomController.this);
					FXUtil.setCellFactory(imageListView, FileCell.class, RoomController.this);
					FXUtil.setCellFactory(strokeWidthBox, StrokeCell.class, RoomController.this);
					FXUtil.setCellFactory(userListView, UserCell.class, RoomController.this);
				}
			});

			strokeWidthBox.setButtonCell(new StrokeCell());
			strokeWidthBox.setItems(FXCollections.observableArrayList(1, 3, 5, 7, 9));
			
			ZoomHandler canvasPaneZoomHandler = new ZoomHandler(canvasPane);
			canvasScrollPane.setOnScroll((event) -> {
				canvasPaneZoomHandler.handle(event);
				double width = Math.max(canvasPane.getBoundsInParent().getWidth(), canvasScrollPane.getWidth());
				double height = Math.max(canvasPane.getBoundsInParent().getHeight(), canvasScrollPane.getHeight());
				double transX = canvasPane.getTranslateX();
				double transY = canvasPane.getTranslateY();
				if(width / 2 < transX) {
					transX = width / 2;
				} else if(transX < 0 && width / 2 < -transX) {
					transX = -width / 2;
				} 
				
				if(height / 2 < transY) {
					transY = height / 2;
				} else if(transY < 0 && height / 2 < -transY) {
					transY = -height / 2;
				} 
				        
				canvasPane.setTranslateX(transX);
				canvasPane.setTranslateY(transY);
		        event.consume();
			});
			
			ZoomHandler imagePaneZoomHandler = new ZoomHandler(imageView);
			imagePane.setOnScroll((event) -> {
				imagePaneZoomHandler.handle(event);
				double width = Math.max(imageView.getBoundsInParent().getWidth(), imagePane.getWidth());
				double height = Math.max(imageView.getBoundsInParent().getHeight(), imagePane.getHeight());
				double transX = imageView.getTranslateX();
				double transY = imageView.getTranslateY();
				if(width / 2 < transX) {
					transX = width / 2;
				} else if(transX < 0 && width / 2 < -transX) {
					transX = -width / 2;
				} 
				
				if(height / 2 < transY) {
					transY = height / 2;
				} else if(transY < 0 && height / 2 < -transY) {
					transY = -height / 2;
				} 
				        
				imageView.setTranslateX(transX);
				imageView.setTranslateY(transY);
		        event.consume();
			});

			musicListView.setOnMouseClicked(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent click) {

					if (click.getClickCount() == 2) {
						FileItem fileItem = musicListView.getSelectionModel().getSelectedItem();
						if (fileItem == null || fileItem.isPhantomProperty().get()
								|| fileItem.isPlayingProperty().get()) {
							return;
						}

						play(fileItem, null, true);
					}
				}
			});
			
			imageListView.setOnMouseClicked(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent click) {

					if (click.getClickCount() == 2) {
						FileItem fileItem = imageListView.getSelectionModel().getSelectedItem();
						if (fileItem == null || fileItem.isPhantomProperty().get()) {
							return;
						}
						
						selectImage(fileItem, true);
					}
				}
			});
			
			fileListView.setOnMouseClicked(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent click) {

					if (click.getClickCount() == 2) {
						FileItem fileItem = fileListView.getSelectionModel().getSelectedItem();
						if (fileItem == null || fileItem.isPhantomProperty().get()) {
							return;
						}

						execute(fileItem);
					}
				}

				
			});
			
			userListView.setOnMouseClicked(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent click) {

					if (click.getClickCount() == 2) {
						User user = userListView.getSelectionModel().getSelectedItem();
						if (user == null || !master.equals(UserHolder.getUser()) || user.equals(UserHolder.getUser())) {
							return;
						}
						if(Dialogs.showConfirm("이 사람에게 제어권을 넘겨주시겠습니까?")) {
							changeMaster(user);	
						}
					}
				}

				
			});

			playSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
				if(currentPlayingItem == null) {
					return;
				}
				if (!isChanging) {
					seek(Duration.seconds(playSlider.getValue()), true);
				}
			});

			playSlider.valueProperty().addListener(new ChangeListener<Number>() {
			    @Override
			    public void changed(ObservableValue<? extends Number> obs, Number oldValue, Number newValue) {
			    	if(currentPlayingItem == null) {
			    		playSlider.setValue(0);
			    		return;
			    	}
			    	
			        if (! playSlider.isValueChanging()) {
			            double currentTime = currentPlayer.currentTimeProperty().get().toSeconds();
			            double sliderTime = newValue.doubleValue();
			            if (Math.abs(currentTime - sliderTime) > 0.5) {
			            	seek(Duration.seconds(newValue.doubleValue()), true);
			            }
			        }
			        
			        double totalSeconds = currentPlayer.getTotalDuration().toSeconds();
			        double seconds = currentPlayer.currentTimeProperty().get().toSeconds();
			        int totalMin = (int) totalSeconds / 60;
			        int totalSec = (int) totalSeconds % 60;
			        int min = (int) seconds / 60;
			        int sec = (int) seconds % 60;
			        playTimeLabel.setText(min + ":" + sec +" / " + totalMin + ":" + totalSec);
			    }
			});
		}
	}
	
	@Override
	public void onCloseRequest(WindowEvent event, boolean softClose) {
		exitRoom();
		getParentController().showWindow();
	}

	
	protected void changeMaster(User user) {
		PacketProcessor.writePacket(RequestPacket.fromCode(PacketCode.CHANGE_MASTER, user));
	}

	private void execute(FileItem fileItem) {
		File file = fileItem.getFile();
		if(!file.exists()) {
			return;
		}
		
		try {
			Desktop.getDesktop().open(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	protected void play(FileItem fileItem, Duration position, boolean sendAction) {
		boolean playable = assureMediaPlayer(fileItem, ()->{
			if(currentPlayer != players.get(fileItem)) {
				stop(false);
			}
			
			currentPlayer = players.get(fileItem);
			currentPlayingItem = fileItem;
			playSlider.setMax(currentPlayer.getTotalDuration().toSeconds());
			
			currentPlayer.play();
			playingLabel.setText(fileItem.getFile().getName());
			if(position != null) {
				if(currentPlayer.getStatus() == Status.PLAYING || currentPlayer.getStatus() == Status.PAUSED) {
					currentPlayer.seek(position);	
				} else {
					currentPlayer.setOnPlaying(()->{
						currentPlayer.setOnPlaying(null);
						currentPlayer.seek(position);	
					});
				}
				
			}
			
			if(sendAction) {
				PlayAction action = new PlayAction(fileItem.getFile().getName(), position);
				if(position == null) {
					action.setPosition(currentPlayer.currentTimeProperty().get());
				}
				sendAction(PacketCode.MEDIA_ACTION, action);	
			}
			playButton.getStyleClass().clear();
			playButton.getStyleClass().add("pause");
		});
		
		if(!playable) {
			Logger.info(fileItem + " is phantom. can't play");
		}
	}

	private void sendAction(PacketCode code, Serializable action) {
		if(!UserHolder.getUser().equals(master)) {
			return;
		}
		
		PacketProcessor.writePacket(RequestPacket.fromCode(code, action));
	}

	private void setDrawingMode(DrawingMode mode) {
		this.drawingMode = mode;

		switch (drawingMode) {
		case NONE:
			moveButton.setSelected(true);
			break;
		case PENCIL:
			pencilButton.setSelected(true);
			break;
		case BRUSH:
			brushButton.setSelected(true);
			break;
		case LINE:
			lineButton.setSelected(true);
			break;
		case RECT:
			rectButton.setSelected(true);
			break;
		case ELLIPSE:
			ellipseButton.setSelected(true);
			break;
		}
	}

	class ZoomHandler implements EventHandler<ScrollEvent> {

		private Node nodeToZoom;

		private ZoomHandler(Node nodeToZoom) {
			this.nodeToZoom = nodeToZoom;
		}

		@Override
		public void handle(ScrollEvent scrollEvent) {
			if (scrollEvent.isControlDown()) {
				final double scale = calculateScale(scrollEvent);
				nodeToZoom.setScaleX(scale);
				nodeToZoom.setScaleY(scale);
				scrollEvent.consume();
			}
		}

		private double calculateScale(ScrollEvent scrollEvent) {
			double scale = nodeToZoom.getScaleX() + scrollEvent.getDeltaY() / 300;

			if (scale <= 0.75) {
				scale = 0.75;
			} else if (scale >= 3) {
				scale = 3;
			}
			return scale;
		}
	}

	private void handleDrawAction(User user, DrawingAction action) {
		List<Node> userNodes = drawingMap.get(user);
		if (userNodes == null) {
			userNodes = new ArrayList<>();
			drawingMap.put(user, userNodes);
		}

		if (action instanceof PathAction) {
			PathAction pathAction = (PathAction) action;
			Path path = new Path();
			path.setStroke(Color.web(FXUtil.intToWeb(pathAction.color)));
			path.setStrokeWidth(pathAction.border);
			for (PathAction.PathElement el : pathAction.getElements()) {
				if (el instanceof PathAction.MoveTo) {
					PathAction.MoveTo mt = (PathAction.MoveTo) el;
					path.getElements().add(new MoveTo(mt.x, mt.y));
				} else if (el instanceof PathAction.LineTo) {
					PathAction.LineTo mt = (PathAction.LineTo) el;
					path.getElements().add(new LineTo(mt.x, mt.y));
				}
			}
			if (((PathAction) action).getBlur() > 0) {
				BoxBlur blur = new BoxBlur();
				blur.setWidth(((PathAction) action).getBlur());
				blur.setHeight(((PathAction) action).getBlur());
				path.setEffect(blur);
			}
			userNodes.add(path);
			canvasPane.getChildren().add(Math.max(0, canvasPane.getChildren().size()), path);
		} else if (action instanceof RectAction) {
			RectAction rectAction = (RectAction) action;
			Rectangle rect = new Rectangle();
			rect.setX(rectAction.x);
			rect.setY(rectAction.y);
			rect.setWidth(rectAction.w);
			rect.setHeight(rectAction.h);
			rect.setStroke(Color.web(FXUtil.intToWeb(rectAction.color)));
			rect.setStrokeWidth(rectAction.border);
			rect.setFill(FXUtil.intToColor(rectAction.fillColor));
			userNodes.add(rect);
			canvasPane.getChildren().add(Math.max(0, canvasPane.getChildren().size()), rect);
		} else if (action instanceof EllipseAction) {
			EllipseAction ellipseAction = (EllipseAction) action;
			Ellipse ellipse = new Ellipse(ellipseAction.cx, ellipseAction.cy, ellipseAction.rx, ellipseAction.ry);
			ellipse.setStroke(Color.web(FXUtil.intToWeb(ellipseAction.color)));
			ellipse.setStrokeWidth(ellipseAction.border);
			ellipse.setFill(FXUtil.intToColor(ellipseAction.fillColor));
			userNodes.add(ellipse);
			canvasPane.getChildren().add(Math.max(0, canvasPane.getChildren().size()), ellipse);
		} else if (action instanceof UndoAction) {
			if (userNodes.size() == 0) {
				Logger.info("wierd undo?");
				return;
			}

			Node lastNode = userNodes.remove(userNodes.size() - 1);
			canvasPane.getChildren().remove(lastNode);
		}
	}

	public void mouseReleased(MouseEvent event) {
		PathAction action = new PathAction();
		switch (drawingMode) {
		case PENCIL:
		case BRUSH:
		case LINE: {
			if (tempPath == null) {
				return;
			}
			action.border = (int) tempPath.getStrokeWidth();
			action.color = FXUtil.colorToInt((Color) tempPath.getStroke());

			for (PathElement el : tempPath.getElements()) {
				if (el instanceof MoveTo) {
					MoveTo mt = (MoveTo) el;
					action.addElement(new PathAction.MoveTo(mt.getX(), mt.getY()));
				} else if (el instanceof LineTo) {
					LineTo mt = (LineTo) el;
					action.addElement(new PathAction.LineTo(mt.getX(), mt.getY()));
				}
			}
			if (drawingMode == DrawingMode.BRUSH) {
				action.setBlur(strokeWidthBox.getValue());
			}
			handleDrawAction(UserHolder.getUser(), action);
			canvasPane.getChildren().remove(tempPath);
			PacketProcessor.writePacket(RequestPacket.fromCode(PacketCode.DRAWING_ACTION, action));
			break;
		}
		case RECT: {
			if (tempRect == null) {
				return;
			}
			RectAction rectAction = new RectAction();
			rectAction.border = (int) tempRect.getStrokeWidth();
			rectAction.x = tempRect.getX();
			rectAction.y = tempRect.getY();
			rectAction.w = tempRect.getWidth();
			rectAction.h = tempRect.getHeight();
			rectAction.color = FXUtil.colorToInt((Color) tempRect.getStroke());
			rectAction.fillColor = FXUtil.colorToInt((Color) tempRect.getFill());
			rectAction.fillType = tempRect.getFill().equals(Color.TRANSPARENT) ? FillType.NONE : FillType.FILL;
			handleDrawAction(UserHolder.getUser(), rectAction);
			canvasPane.getChildren().remove(tempRect);
			PacketProcessor.writePacket(RequestPacket.fromCode(PacketCode.DRAWING_ACTION, rectAction));
			break;
		}
		case ELLIPSE: {
			if (tempEllipse == null) {
				return;
			}
			EllipseAction ellipseAction = new EllipseAction();
			ellipseAction.border = (int) tempEllipse.getStrokeWidth();
			ellipseAction.cx = tempEllipse.getCenterX();
			ellipseAction.cy = tempEllipse.getCenterY();
			ellipseAction.rx = tempEllipse.getRadiusX();
			ellipseAction.ry = tempEllipse.getRadiusY();
			ellipseAction.color = FXUtil.colorToInt((Color) tempEllipse.getStroke());
			ellipseAction.fillColor = FXUtil.colorToInt((Color) tempEllipse.getFill());
			ellipseAction.fillType = tempEllipse.getFill().equals(Color.TRANSPARENT) ? FillType.NONE : FillType.FILL;
			handleDrawAction(UserHolder.getUser(), ellipseAction);
			canvasPane.getChildren().remove(tempEllipse);
			PacketProcessor.writePacket(RequestPacket.fromCode(PacketCode.DRAWING_ACTION, ellipseAction));
			break;
		}
		default: {

		}
		}
	}
	
	public void onImagePressed(MouseEvent event) {
		anchorPoint = new Point2D(event.getX(), event.getY());
	}
	
	public void onImageDragged(MouseEvent event) {
			double width = Math.max(imageView.getBoundsInParent().getWidth(), imagePane.getWidth());
			double height = Math.max(imageView.getBoundsInParent().getHeight(), imagePane.getHeight());
			double transX = imageView.getTranslateX() + event.getX() - anchorPoint.getX();
			double transY = imageView.getTranslateY() + event.getY() - anchorPoint.getY();
			if(width / 2 < transX) {
				transX = width / 2;
			} else if(transX < 0 && width / 2 < -transX) {
				transX = -width / 2;
			} 
			
			if(height / 2 < transY) {
				transY = height / 2;
			} else if(transY < 0 && height / 2 < -transY) {
				transY = -height / 2;
			} 
			        
			imageView.setTranslateX(transX);
			imageView.setTranslateY(transY);
	        event.consume();
	        return;
	}

	public void mouseDragged(MouseEvent event) {
		if (event.getX() < 0 || event.getX() > canvasPane.getWidth() || event.getY() < 0
				|| event.getY() > canvasPane.getHeight()) {
			return;
		}

		if(drawingMode == DrawingMode.NONE) {
			double width = Math.max(canvasPane.getBoundsInParent().getWidth(), canvasScrollPane.getWidth());
			double height = Math.max(canvasPane.getBoundsInParent().getHeight(), canvasScrollPane.getHeight());
			double transX = canvasPane.getTranslateX() + event.getX() - anchorPoint.getX();
			double transY = canvasPane.getTranslateY() + event.getY() - anchorPoint.getY();
			if(width / 2 < transX) {
				transX = width / 2;
			} else if(transX < 0 && width / 2 < -transX) {
				transX = -width / 2;
			} 
			
			if(height / 2 < transY) {
				transY = height / 2;
			} else if(transY < 0 && height / 2 < -transY) {
				transY = -height / 2;
			} 
			        
			canvasPane.setTranslateX(transX);
			canvasPane.setTranslateY(transY);
	        event.consume();
	        return;
		}

		        
		boolean isPrimary = event.isPrimaryButtonDown();
		Color color1 = isPrimary ? majorColorPicker.getValue() : minorColorPicker.getValue();
		Color color2 = !isPrimary ? majorColorPicker.getValue() : minorColorPicker.getValue();

		if (startDrag) {
			switch (drawingMode) {
			case PENCIL: {
				tempPath = new Path();
				tempPath.setStroke(color1);
				tempPath.setStrokeWidth(strokeWidthBox.getValue());
				MoveTo mt = new MoveTo(event.getX(), event.getY());
				tempPath.getElements().add(mt);
				canvasPane.getChildren().add(tempPath);
				break;
			}
			case BRUSH: {
				BoxBlur blur = new BoxBlur();
				blur.setWidth(strokeWidthBox.getValue());
				blur.setHeight(strokeWidthBox.getValue());
				tempPath = new Path();
				MoveTo m = new MoveTo(event.getX(), event.getY());
				tempPath.setStroke(color1);
				tempPath.setStrokeType(StrokeType.CENTERED);
				tempPath.setStrokeLineCap(StrokeLineCap.ROUND);
				tempPath.setStrokeWidth(strokeWidthBox.getValue());
				tempPath.getElements().add(m);
				tempPath.setEffect(blur);
				canvasPane.getChildren().add(tempPath);
				break;
			}
			case LINE: {
				tempPath = new Path();
				tempPath.setStroke(color1);
				tempPath.setStrokeWidth(strokeWidthBox.getValue());
				tempPath.setStrokeLineCap(StrokeLineCap.ROUND);
				MoveTo mt = new MoveTo(anchorPoint.getX(), anchorPoint.getY());
				LineTo lt = new LineTo(event.getX(), event.getY());
				tempPath.getElements().add(mt);
				tempPath.getElements().add(lt);
				canvasPane.getChildren().add(tempPath);
				break;
			}
			case RECT: {

				tempRect = new Rectangle(anchorPoint.getX(), anchorPoint.getY(), event.getX() - anchorPoint.getX(),
						event.getY() - anchorPoint.getY());
				tempRect.setStroke(color1);
				tempRect.setStrokeWidth(strokeWidthBox.getValue());
				if (fillCheck.isSelected()) {
					tempRect.setFill(color2);
				} else {
					tempRect.setFill(Color.TRANSPARENT);
				}
				canvasPane.getChildren().add(tempRect);
				break;
			}
			case ELLIPSE: {
				double centerX = anchorPoint.getX() + (anchorPoint.getX() - event.getX()) / 2;
				double centerY = anchorPoint.getY() + (anchorPoint.getY() - event.getY()) / 2;

				tempEllipse = new Ellipse(centerX, centerY, centerX - anchorPoint.getX(), centerY - anchorPoint.getY());
				tempEllipse.setStroke(color1);
				tempEllipse.setStrokeWidth(strokeWidthBox.getValue());
				if (fillCheck.isSelected()) {
					tempEllipse.setFill(color2);
				} else {
					tempEllipse.setFill(Color.TRANSPARENT);
				}
				canvasPane.getChildren().add(tempEllipse);
				break;
			}
			default: {

			}
			}
		}

		startDrag = false;
		switch (drawingMode) {
		case PENCIL: {
			LineTo lt = new LineTo(event.getX(), event.getY());
			tempPath.getElements().add(lt);
			break;
		}
		case BRUSH: {
			LineTo lt = new LineTo(event.getX(), event.getY());
			tempPath.getElements().add(lt);
			break;
		}
		case LINE: {
			LineTo lt = (LineTo) tempPath.getElements().get(1);
			lt.setX(event.getX());
			lt.setY(event.getY());
			break;
		}
		case RECT: {
			// LU: ex, ey, ax-ex, ay-ey
			// RU: ax, ey, ex-ax, ay-ey
			// LD: ex, ay, ax-ex, ey-ay
			// RD: ax, ay, ex-ax, ey-ay
			double ax = anchorPoint.getX();
			double ay = anchorPoint.getY();
			double ex = event.getX();
			double ey = event.getY();

			if (ax > ex && ay > ey) {
				tempRect.setX(ex);
				tempRect.setY(ey);
				tempRect.setWidth(ax - ex);
				tempRect.setHeight(ay - ey);
			} else if (ex > ax && ay > ey) {
				tempRect.setX(ax);
				tempRect.setY(ey);
				tempRect.setWidth(ex - ax);
				tempRect.setHeight(ay - ey);
			} else if (ax > ex && ey > ay) {
				tempRect.setX(ex);
				tempRect.setY(ay);
				tempRect.setWidth(ax - ex);
				tempRect.setHeight(ey - ay);
			} else if (ex > ax && ey > ay) {
				tempRect.setX(ax);
				tempRect.setY(ay);
				tempRect.setWidth(ex - ax);
				tempRect.setHeight(ey - ay);
			}
			break;
		}
		case ELLIPSE: {
			double ax = anchorPoint.getX();
			double ay = anchorPoint.getY();
			double ex = event.getX();
			double ey = event.getY();

			double rx = Math.abs(ax - ex) / 2;
			double ry = Math.abs(ay - ey) / 2;
			tempEllipse.setRadiusX(rx);
			tempEllipse.setRadiusY(ry);

			if (ax > ex && ay > ey) {
				tempEllipse.setCenterX(ex + rx);
				tempEllipse.setCenterY(ey + ry);
			} else if (ex > ax && ay > ey) {
				tempEllipse.setCenterX(ax + rx);
				tempEllipse.setCenterY(ey + ry);
			} else if (ax > ex && ey > ay) {
				tempEllipse.setCenterX(ex + rx);
				tempEllipse.setCenterY(ay + ry);
			} else if (ex > ax && ey > ay) {
				tempEllipse.setCenterX(ax + rx);
				tempEllipse.setCenterY(ay + ry);
			}
			break;
		}
		default:

		}

	}

	public void mousePressed(MouseEvent event) {
		if (event.getX() < 0 || event.getX() > canvasPane.getWidth() || event.getY() < 0
				|| event.getY() > canvasPane.getHeight()) {
			return;
		}

		anchorPoint = new Point2D(event.getX(), event.getY());
		startDrag = true;
		tempPath = null;
		tempRect = null;
		tempEllipse = null;
	}

	public static String toRGBCode(Color color) {
		return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255),
				(int) (color.getBlue() * 255));
	}

	public void onSend() {
		if(messageField.getText().isEmpty()) {
			return;
		}
			
		ChatAction action = new ChatAction();
		action.setMessage(messageField.getText());
		PacketProcessor.writePacket(RequestPacket.fromCode(PacketCode.CHAT_ACTION, action));
		messageField.setText("");
	}

	private static String toSizeString(long size) {
		if (size >= 1024 * 1024 * 1024) {
			return String.format("%.2f%s", (double) size / (1024 * 1024 * 1024), "GB");
		} else if (size >= 1024 * 1024) {
			return String.format("%.2f%s", (double) size / (1024 * 1024), "MB");
		} else if (size >= 1024) {
			return String.format("%.2f%s", (double) size / (1024), "KB");
		} else {
			return size + "B";
		}
	}

	private static long fromSizeString(String size) {
		if (size.endsWith("GB")) {
			return (long) (Double.parseDouble(size.substring(0, size.length() - 2)) * 1024 * 1024 * 1024);
		} else if (size.endsWith("MB")) {
			return (long) (Double.parseDouble(size.substring(0, size.length() - 2)) * 1024 * 1024);
		} else if (size.endsWith("KB")) {
			return (long) (Double.parseDouble(size.substring(0, size.length() - 2)) * 1024);
		} else if (size.endsWith("KB")) {
			return (long) (Double.parseDouble(size.substring(0, size.length() - 2)));
		} else {
			return 0;
		}
	}

	private static String toFileURI(File file) {
		return "file:///" + file.getAbsolutePath().replace("\\", "/").replace(" ", "%20");
	}

	public void onClick(Event event) {
		Object src = event.getSource();

		if (src == moveButton) {
			setDrawingMode(DrawingMode.NONE);
		} else if (src == pencilButton) {
			drawingMode = DrawingMode.PENCIL;
			pencilButton.setSelected(true);
			event.consume();
		} else if (src == brushButton) {
			drawingMode = DrawingMode.BRUSH;
			brushButton.setSelected(true);
			event.consume();
		} else if (src == rectButton) {
			drawingMode = DrawingMode.RECT;
			rectButton.setSelected(true);
			event.consume();
		} else if (src == lineButton) {
			drawingMode = DrawingMode.LINE;
			lineButton.setSelected(true);
			event.consume();
		} else if (src == ellipseButton) {
			drawingMode = DrawingMode.ELLIPSE;
			ellipseButton.setSelected(true);
			event.consume();
		} else if (src == undoButton) {
			List<Node> myNodes = drawingMap.get(UserHolder.getUser());
			if (myNodes != null && myNodes.size() > 0) {
				PacketProcessor.writePacket(RequestPacket.fromCode(PacketCode.DRAWING_ACTION, new UndoAction()));
				handleDrawAction(UserHolder.getUser(), new UndoAction());
			}
		} else if (src == exitButton) {
			closeWindow();
		} else if (src instanceof Button && ((Button) src).getStyleClass().contains("add")) {
			FileType type = null;
			type = src == musicAddButton ? FileType.MUSIC : src == imageAddButton ? FileType.IMAGE : FileType.FILE;

			FileChooser chooser = new FileChooser();
			File file = chooser.showOpenDialog(getStage());
			if (src == musicAddButton) {
				chooser.getExtensionFilters().setAll(new ExtensionFilter("Music File", "mp3", "wav"));
			} else if (src == imageAddButton) {
				chooser.getExtensionFilters()
						.setAll(new ExtensionFilter("Image File", "jpg", "bmp", "png", "gif", "jpeg"));
			} else {
				chooser.getExtensionFilters().clear();
			}

			uploadFile(file, type);

		} else if (src == musicRemoveButton || src == imageRemoveButton || src == fileRemoveButton) {
			FileType type = null;

			if (src == musicRemoveButton) {
				type = FileType.MUSIC;
			} else if (src == imageRemoveButton) {
				type = FileType.IMAGE;
			} else if (src == fileRemoveButton) {
				type = FileType.FILE;
			}

			ListView<FileItem> listView = getListView(type);
			FileItem item = listView.getSelectionModel().getSelectedItem();
			if (item == null) {
				return;
			}
			FileKey fileKey = new FileKey(item.getFile().getName());
			fileKey.setType(type);
			PacketProcessor.writePacket(RequestPacket.fromCode(PacketCode.FILE_REMOVED, fileKey));
			
			if(item.isProgressingProperty().get()) {
				if(uploadTasks.containsKey(fileKey)) {
					uploadTasks.get(fileKey).interrupt();
				}
				if(downloadTasks.containsKey(fileKey)) {
					downloadTasks.get(fileKey).interrupt();
				}
			}
		} else if(src == captureButton) {
			captureController.showWindow(true);
			File file = captureController.getSaved();
			if(file != null) {
				uploadFile(file, FileType.IMAGE);
			}
			
		} else if (src == playButton) {
			if(currentPlayer == null) {
				return;
			}
			
			if(currentPlayer.getStatus() != Status.PLAYING) {
				play(currentPlayingItem, null, true);	
			} else {
				pause(true);
			}
		} else if (src == nextButton) {
			playNext();
		} else if (src == prevButton) {
			playPrev();
		} else if (src == messageIcon) {
			onSend();
		} else if(src instanceof Node && ((Node) src).getStyleClass().contains("open")) {
			File dir = ClientStorageManager.getLocalFile(room, null);
			try {
				if(dir.exists()){
					Desktop.getDesktop().open(dir);	
				} else {
					Dialogs.showAlert("다른사람의 파일을 한개이상 받으세요");
				}
			} catch (IOException e) {
				Logger.error(e);
			}
		}
	}

	private void uploadFile(File file, FileType type) {
		if (file == null) {
			return;
		}
		if(type.equals(FileType.MUSIC) && !(file.getName().endsWith("mp3") || file.getName().endsWith("wav"))) {
			Dialogs.showAlert("음악파일을 올려주세요");
			return;
		} else if(type.equals(FileType.IMAGE) && !(file.getName().endsWith("jpg") || file.getName().endsWith("png") || file.getName().endsWith("bmp") || file.getName().endsWith("gif") || file.getName().endsWith("jpeg"))) {
			Dialogs.showAlert("사진파일을 올려주세요");
			return;
		}
		
		final FileItem fileItem = new FileItem(file, null);
		// fileItem.setDate(new Date());
		fileItem.setUser(UserHolder.getUser());

		ListView<FileItem> listView = getListView(type);
		fileItem.setType(type);

		if (listView.getItems().contains(fileItem)) {
			Dialogs.showAlert("파일 명이 중복됩니다.");
			return;
		} else {
			listView.getItems().add(fileItem);
		}

		fileItem.isProgressingProperty().set(true);
		FileKey fileKey = new FileKey(fileItem.getFile().getName(), type);
		uploadTasks.put(fileKey, PacketProcessor.uploadFile(type, room, file, new ProgressHandler() {
			@Override
			public void inProgress(long total, long current) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						double percent = ((current / (double) total));
						fileItem.progressProperty().set(percent);
						fileItem.sizeProperty().set(current);
						if (total == current) {
							fileItem.isProgressingProperty().set(false);
							fileItem.isPhantomProperty().set(false);
							// listView.refresh();
							uploadTasks.remove(fileKey);
						}
					}
				});
			}

			@Override
			public void onError(Throwable e) {
				Logger.error(e);
				fileItem.isProgressingProperty().set(false);
				uploadTasks.remove(fileKey);
			}
		}));
	}

	private void exitRoom() {
		stop(true);
		for(FileItem fileItem : musicListView.getItems()) {
			clearMusic(fileItem);	
		}
		
		for(FileTransferTask task : downloadTasks.values()) {
			task.interrupt();
		}
		
		for(FileTransferTask task : uploadTasks.values()) {
			task.interrupt();
		}
		
		PacketProcessor.writePacket(RequestPacket.fromCode(PacketCode.EXIT_ROOM));
	}

	private void playNext() {
		if (currentPlayingItem == null) {
			return;
		}
		int playingIndex = musicListView.getItems().indexOf(currentPlayingItem);
		if (playingIndex == musicListView.getItems().size() - 1) {
			stop(true);
			return;
		}

		FileItem item = musicListView.getItems().get(playingIndex + 1);
		play(item, null, true);
	}
	
	

	private void playPrev() {
		if (currentPlayingItem == null) {
			return;
		}
		int playingIndex = musicListView.getItems().indexOf(currentPlayingItem);
		if (playingIndex <= 0) {
			stop(true);
			return;
		}

		FileItem item = musicListView.getItems().get(playingIndex - 1);
		play(item, null, true);
	}

	@Override
	public void handlePacket(ResponsePacket packet) {
		switch (packet.getCode()) {
		case CHAT_ACTION: {
			ParamMap map = (ParamMap) packet.getData();
			User user = (User) map.get("user");
			ChatAction action = (ChatAction) map.get("chat");
			chatArea.appendText(user.getNickname() + " : " + action.getMessage() + "\n");
			break;
		}
		case FILE_ADDED:
		case FILE_REMOVED: {
			FileKey fileKey = (FileKey) packet.getData();
			FileItem item = new FileItem(ClientStorageManager.getLocalFile(room, fileKey.getName()), fileKey.getType());
			ListView<FileItem> listView = getListView(fileKey.getType());

			if (packet.getCode() == PacketCode.FILE_ADDED) {
				if (listView.getItems().contains(item)) {
					item = listView.getItems().get(listView.getItems().indexOf(item));
					item.isPhantomProperty().set(false);
					Logger.info(fileKey + " uploaded");
				} else {
					listView.getItems().add(item);
					item.isPhantomProperty().set(true);
					Logger.info(fileKey + " added");
				}

				item.sizeProperty().set(fileKey.getSize());
				item.setDate(fileKey.getDate());
				item.setUser(fileKey.getUser());

				getListView(item.getType()).refresh();
			} else if (packet.getCode() == PacketCode.FILE_REMOVED) {
				listView.getItems().remove(item);
				if(fileKey.getType() == FileType.MUSIC) {
					clearMusic(item);
				}
				if(fileKey.getType() == FileType.IMAGE) {
					if(currentSelectedImage != null && currentSelectedImage.equals(item)) {
						selectImage(null, false);
					}
				}
				Logger.info(fileKey + " removed");
			}

			break;
		}
		case DRAWING_ACTION: {
			ParamMap param = (ParamMap) packet.getData();
			handleDrawAction((User) param.get("user"), (DrawingAction) param.get("action"));
			break;
		}
		case MEDIA_ACTION: {
			ParamMap param = (ParamMap) packet.getData();
			handleMusicAction((User) param.get("user"), (MusicAction) param.get("action"));
			break;
		}
		case IMAGE_ACTION: {
			ParamMap param = (ParamMap) packet.getData();
			handleImageAction((User) param.get("user"), (ImageAction) param.get("action"));
			break;
		}
		case USER_ENTER: {
			User user = (User) packet.getData();
			if (!userListView.getItems().contains(user)) {
				userListView.getItems().add(user);
			}
			break;
		}
		case EXIT_ROOM: {
			User user = (User) packet.getData();
			userListView.getItems().remove(user);
			break;
		}
		case HEAR_ACTION: {
			ParamMap param = (ParamMap) packet.getData();
			User user = (User) param.get("user");
			Boolean hearFromFlag = (Boolean) param.get("hearFrom");
			hearFrom.put(user, hearFromFlag);
			userListView.refresh();
			if(hearFromFlag && user.equals(UserHolder.getUser())) {
				syncImage();
				syncMusic();
			}
			if(!master.equals(UserHolder.getUser())) {
				musicTab.getContent().setDisable(hearFromFlag);
				imageToolbar.setDisable(hearFromFlag);
				imageListView.setDisable(hearFromFlag);
			} else {
				musicTab.getContent().setDisable(false);
				imageToolbar.setDisable(false);
				imageListView.setDisable(false);
			}
			break;
		}
		case CHANGE_MASTER: {
			User user = (User) packet.getData();
			master = user;
			boolean hearFromFlag = hearFrom.get(UserHolder.getUser()).booleanValue();
			if(master.equals(UserHolder.getUser()) || hearFromFlag == true) {
				stop(false);
				if(!master.equals(UserHolder.getUser())) {
					musicTab.getContent().setDisable(hearFromFlag);
					imageToolbar.setDisable(hearFromFlag);
					imageListView.setDisable(hearFromFlag);
				} else {
					musicTab.getContent().setDisable(false);
					imageToolbar.setDisable(false);
					imageListView.setDisable(false);
				}
			}
			userListView.refresh();
		}
		default:
			break;
		}
	}

	private void clearMusic(FileItem fileItem) {
		if(currentPlayingItem != null && currentPlayingItem.equals(fileItem)) {
			currentPlayer.stop();
			currentPlayer.dispose();
			currentPlayer = null;
			currentPlayingItem = null;	
		} else {
			MediaPlayer mp = players.get(fileItem);
			if(mp != null) {
				mp.dispose();	
			}
		}
		
		players.remove(fileItem);
	}

	private void handleImageAction(User user, ImageAction imageAction) {
		imageSync.applyImageAction(imageAction);
		
		if(isHearFrom() == false) {
			return;
		}
		
		FileItem item = new FileItem(new File(imageAction.getImageKey().getName()), FileType.IMAGE);
		int index = imageListView.getItems().indexOf(item);
		if(index != -1 && !(item = imageListView.getItems().get(index)).isPhantomProperty().get()) {
			selectImage(item, false);	
		}
	}

	private boolean isHearFrom() {
		return Boolean.TRUE.equals(hearFrom.get(UserHolder.getUser()));
	}

	private void handleMusicAction(User user, MusicAction musicAction) {
		musicSync.applyMusicAction(musicAction);

		if(isHearFrom() == false) {
			return;
		}
		
		FileKey fileKey = musicAction.getFileKey();
		FileItem item = new FileItem(new File(fileKey.getName()), FileType.MUSIC);
		int index = -1;
		if((index = musicListView.getItems().indexOf(item)) > -1) {
			item = musicListView.getItems().get(index);
			if(item.isPhantomProperty().get()) {
				Logger.info("not downloaded music ! " + musicAction);
				return;
			}
		}
		if (musicAction instanceof PlayAction) {
			play(item, musicAction.getPosition(), false);
		} else if (musicAction instanceof PauseAction) {
			pause(false);
		} else if (musicAction instanceof StopAction) {
			stop(false);
		} else if (musicAction instanceof SeekAction) {
			seek(musicAction.getPosition(), false);
		}
	}

	private void seek(Duration millis, boolean sendAction) {
		if(currentPlayingItem == null) {
			return;
		}
		currentPlayer.seek(millis);
		
		if(sendAction) {
			SeekAction action = new SeekAction(currentPlayingItem.getFile().getName(), millis);
			sendAction(PacketCode.MEDIA_ACTION, action);	
		}
	}

	private void stop(boolean sendAction) {
		if(currentPlayingItem == null) {
			return;
		}
		
		if(sendAction) {
			StopAction action = new StopAction(currentPlayingItem.getFile().getName());
			sendAction(PacketCode.MEDIA_ACTION, action);	
		}
		
		currentPlayer.stop();
		currentPlayer = null;
		currentPlayingItem = null;
		playSlider.setValue(0);
		playButton.getStyleClass().clear();
		playingLabel.setText("");
		playTimeLabel.setText("00:00 / 00:00");
	}

	private void pause(boolean sendAction) {
		if(currentPlayingItem == null) {
			return;
		}
		playButton.getStyleClass().clear();
		currentPlayer.pause();
		
		if(sendAction) {
			PauseAction action = new PauseAction(currentPlayingItem.getFile().getName(), currentPlayer.currentTimeProperty().get());
			sendAction(PacketCode.MEDIA_ACTION, action);	
		}
	}

	private boolean assureMediaPlayer(FileItem item, Runnable action) {
		try {
			int index = musicListView.getItems().indexOf(item);
			if (index > -1 && !item.isPhantomProperty().get()) {
				if (!players.containsKey(item)) {
					final MediaPlayer player = new MediaPlayer(new Media(toFileURI(item.getFile())));
					System.out.println(player + " initialized");
					player.setOnReady(action);
					player.setOnEndOfMedia(()->{
						playNext();
					});
					player.totalDurationProperty()
							.addListener((obs, oldDuration, newDuration) -> playSlider.setMax(newDuration.toSeconds()));

					player.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
						if (!playSlider.isValueChanging()) {
							playSlider.setValue(player.currentTimeProperty().get().toSeconds());
						}
					});
					
					players.put(item, player);
				} else {
					action.run();
				}
				
				return true;
			}
		} catch (Exception e) {
			Logger.error(e);
		}

		return false;
	}

	private ListView<FileItem> getListView(FileType type) {
		switch (type) {
		case IMAGE:
			return imageListView;
		case MUSIC:
			return musicListView;
		case FILE:
			return fileListView;
		}
		return null;
	}

	public static class FileItem {
		private File file;
		private FileType type;
		private Date date;
		private User user;

		private BooleanProperty isProgressingProperty;
		private BooleanProperty isPlayingProperty;
		private BooleanProperty isPhantomProperty;
		private DoubleProperty progressProperty;
		private LongProperty sizeProperty;

		FileItem(File file, FileType type) {
			this.date = new Date();
			this.user = new User();
			this.file = file;
			this.type = type;
			this.progressProperty = new SimpleDoubleProperty((double) 0);
			this.sizeProperty = new SimpleLongProperty(0);
			this.isPlayingProperty = new SimpleBooleanProperty(false);
			this.isProgressingProperty = new SimpleBooleanProperty(false);
			this.isPhantomProperty = new SimpleBooleanProperty(true);
		}

		public FileType getType() {
			return type;
		}

		public void setType(FileType type) {
			this.type = type;
		}

		public File getFile() {
			return file;
		}

		public void setFile(File file) {
			this.file = file;
		}

		public User getUser() {
			return user;
		}

		public void setUser(User user) {
			this.user = user;
		}

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}

		public LongProperty sizeProperty() {
			return sizeProperty;
		}

		public BooleanProperty isProgressingProperty() {
			return isProgressingProperty;
		}

		public BooleanProperty isPlayingProperty() {
			return isPlayingProperty;
		}

		public BooleanProperty isPhantomProperty() {
			return isPhantomProperty;
		}

		public DoubleProperty progressProperty() {
			return progressProperty;
		}

		@Override
		public boolean equals(Object obj) {
			FileItem item = (FileItem) obj;
			if (item == null || item.file == null || file == null || item.type == null || type == null) {
				return false;
			}

			if (item != null && item.file.getName().equals(file.getName()) && item.type.equals(type)) {
				return true;
			}

			return false;
		}

		@Override
		public int hashCode() {
			if (type == null || file == null) {
				return -1;
			}

			return (type.toString() + file.getName()).hashCode();
		}
	}

	public class FileCell extends CustomListCell<FileItem> {

		private SimpleDateFormat dateFormat = new SimpleDateFormat("a h시 m분");
		@FXML
		Label fileUserTime, fileName, fileSize;

		@FXML
		ProgressIndicator progressIndicator;

		@FXML
		ImageView fileIcon, downPlayingIcon;

		public FileCell() {
			super("fileCell.fxml");
		}

		@Override
		protected void drawCell(FileItem item, Node node) {
			progressIndicator.visibleProperty().bind(item.isProgressingProperty());
			progressIndicator.progressProperty().bind(item.progressProperty());
			Bindings.bindBidirectional(fileSize.textProperty(), item.sizeProperty(), new StringConverter<Number>() {
				@Override
				public Number fromString(String string) {
					return fromSizeString(string);
				}

				@Override
				public String toString(Number object) {
					return toSizeString(object.longValue());
				};
			});

			fileIcon.getStyleClass().clear();
			fileIcon.getStyleClass().add(item.getType().toString());
			fileUserTime.setText(String.format("%s, %s", item.getUser().getNickname(),
					item.getDate() == null ? "" : dateFormat.format(item.getDate())));
			fileName.setText(item.getFile().getName());

			InvalidationListener downPlayChanged = (observable) -> {
				downPlayingIcon.getStyleClass().clear();
				if (item.isPlayingProperty().get() && item.getType().equals(FileType.MUSIC)) {
					downPlayingIcon.getStyleClass().add("playing");
				} else if (item.isPhantomProperty().get()) {
					downPlayingIcon.getStyleClass().add("down");
				}
			};
			downPlayChanged.invalidated(null);
			item.isPlayingProperty().addListener(downPlayChanged);
			item.isPhantomProperty().addListener(downPlayChanged);

			downPlayingIcon.visibleProperty().bind(item.isProgressingProperty().not());
			downPlayingIcon.setUserData(item);
		}

		public void onClick(Event event) {
			if (event.getSource() == downPlayingIcon && downPlayingIcon.getStyleClass().contains("down")) {
				final FileItem fileItem = (FileItem) downPlayingIcon.getUserData();
				fileItem.isProgressingProperty().set(true);
				FileKey fileKey = new FileKey(fileItem.getFile().getName(), fileItem.getType());
				
				downloadTasks.put(fileKey, PacketProcessor.downloadFile(room, fileItem.getType(), fileItem.getFile(), new ProgressHandler() {
					@Override
					public void inProgress(long total, long current) {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								fileItem.progressProperty().set(((current / (double) total)));
								fileItem.sizeProperty().set(current);
								if (total == current) {
									fileItem.isProgressingProperty().set(false);
									fileItem.isPhantomProperty().set(false);
									downloadTasks.remove(fileKey);
								}
							}
						});
					}

					public void onError(Throwable e) {
						Logger.error(e);
						fileItem.isProgressingProperty().set(false);
						downloadTasks.remove(fileKey);
					}
				}));
			}
		}
	}

	public class StrokeCell extends CustomListCell<Integer> {

		public StrokeCell() {
			super(new Path());
			Path path = (Path) this.node;
			path.setId("line");
			path.getElements().add(new MoveTo(0, 5));
			path.getElements().add(new LineTo(15, 5));
			path.setStroke(Color.BLACK);
			path.setStrokeLineCap(StrokeLineCap.ROUND);
		}

		@Override
		protected void drawCell(Integer item, Node node) {
			Path path = (Path) node;
			path.setStrokeWidth(item);
		}
	}

	public class UserCell extends CustomListCell<User> {
		@FXML
		Label nickname, hearFrom;
		@FXML
		ImageView userIcon;

		public UserCell() {
			super("userCell.fxml");
		}

		protected void drawCell(User item, Node node) {
			nickname.setText(item.getNickname());
			Map<User, Boolean> hearFromMap = RoomController.this.hearFrom;
			Boolean flag = hearFromMap.get(item);
			userIcon.getStyleClass().clear();
			userIcon.setUserData(item);
			if (item.equals(master)) {
				hearFrom.setText("제어중");
				userIcon.getStyleClass().add("master");
			} else if (flag == null || flag == false) {
				hearFrom.setText("");
				if (UserHolder.getUser().equals(item)) {
					userIcon.getStyleClass().add("me");
				}
			} else {
				hearFrom.setText("청취중..");
				userIcon.getStyleClass().add("hear");
			}
		}

		public void onClick(Event event) {
			if (event.getSource() == userIcon) {
				User user = (User) userIcon.getUserData();
				if (UserHolder.getUser().equals(user) && !master.equals(user)) {
					Boolean flag = RoomController.this.hearFrom.get(user);
					if (flag == null) {
						flag = false;
					}
					PacketProcessor.writePacket(RequestPacket.fromCode(PacketCode.HEAR_ACTION, !flag));
				}
			}
		}
	}

	public void initRoom(Room room, List<User> list, RoomMeta roomMeta, User master) {
		this.master = master;
		this.room = room;
		this.musicSync = roomMeta.getMusicSync();
		this.imageSync = roomMeta.getImageSync();
		this.roomTitle.setText(room.getTitle());
		this.fillCheck.setSelected(false);
		this.startDrag = false;
		this.currentPlayer = null;
		this.currentPlayingItem = null;
		this.players.clear();
		this.drawingMap.clear();
		this.drawingMap.put(UserHolder.getUser(), new ArrayList<Node>());
		this.canvasPane.getChildren().clear();
		this.setDrawingMode(DrawingMode.PENCIL);
		this.majorColorPicker.setValue(Color.BLACK);
		this.strokeWidthBox.getSelectionModel().clearAndSelect(0);
		this.hearFrom.clear();
		this.hearFrom.putAll(roomMeta.getHearFrom());
		if(this.hearFrom.get(UserHolder.getUser()) && !master.equals(UserHolder.getUser())) {
			imageToolbar.setDisable(true);
			imageListView.setDisable(true);
			musicTab.getContent().setDisable(true);
		} else {
			imageToolbar.setDisable(false);
			imageListView.setDisable(false);
			musicTab.getContent().setDisable(false);
		}
		this.userListView.setItems(FXCollections.observableList(list));
		this.userListView.refresh();
		this.imageView.setImage(null);
		
		canvasPane.setScaleX(1);
		canvasPane.setScaleY(1);
		canvasPane.setTranslateX(0);
		canvasPane.setTranslateY(0);
		
		if(captureController == null) {
			captureController = new CaptureController(this, room);
		} else {
			captureController.setRoom(room);
		}
		
		this.tabPane.getSelectionModel().clearAndSelect(0);
		
		Function<FileType, Function<FileKey, FileItem>> applyFunc = (type) -> {
			return (fileKey) -> {
				FileItem item = new FileItem(ClientStorageManager.getLocalFile(room, fileKey.getName()), null);
				item.setType(type);
				item.setUser(fileKey.getUser());
				item.setDate(fileKey.getDate());
				item.sizeProperty().set(fileKey.getSize());
				return item;
			};
		};

		Map<User, List<DrawingAction>> actionMap = roomMeta.getDrawingSync().getActionMap();
		for (Entry<User, List<DrawingAction>> entry : actionMap.entrySet()) {
			for (DrawingAction action : entry.getValue()) {
				handleDrawAction(entry.getKey(), action);
			}
		}

		fileListView.setItems(FXCollections.observableArrayList(
				roomMeta.getFileList().stream().map(applyFunc.apply(FileType.FILE)).collect(Collectors.toList())));
		imageListView.setItems(FXCollections.observableArrayList(
				roomMeta.getImageList().stream().map(applyFunc.apply(FileType.IMAGE)).collect(Collectors.toList())));
		musicListView.setItems(FXCollections.observableArrayList(
				roomMeta.getMusicList().stream().map(applyFunc.apply(FileType.MUSIC)).collect(Collectors.toList())));
		musicListView.refresh();
		localFileCheck(fileListView.getItems());
		localFileCheck(imageListView.getItems());
		localFileCheck(musicListView.getItems());
	}

	private void syncMusic() {
		if(musicSync.getFileKey() == null) {
			return;
		}
		
		FileKey fileKey = musicSync.getFileKey();
		FileItem item = new FileItem(ClientStorageManager.getLocalFile(room, fileKey.getName()), FileType.MUSIC);
		int index = musicListView.getItems().indexOf(item);
		if(index != -1 && !(item = musicListView.getItems().get(index)).isPhantomProperty().get()) {
			if(musicSync.isPlaying()) {
				long delta = System.currentTimeMillis() - musicSync.getLastModified().get();
				play(item, Duration.millis(musicSync.getLastPosition().get() + delta), false);	
			} else {
				stop(false);
			}
		} else {
			Logger.info("Can't sync. Current music that needs sync is phantom");
		}
	}
	
	private void syncImage() {
		if(imageSync.getFileKey() == null) {
			return;
		}
		
		FileKey fileKey = imageSync.getFileKey();
		FileItem item = new FileItem(ClientStorageManager.getLocalFile(room, fileKey.getName()), FileType.IMAGE);
		int index = imageListView.getItems().indexOf(item);
		if(index != -1 && !(item = imageListView.getItems().get(index)).isPhantomProperty().get()) {
			item = imageListView.getItems().get(index);
			selectImage(item, false);
		} else {
			Logger.info("Can't sync. Current image that needs sync is phantom");
		}
	}

	private void selectImage(FileItem item, boolean sendAction) {
		if(item == null) {
			imageView.setImage(null);
			return;
		}
		if(!item.isPhantomProperty().get()) {
			imageView.setImage(new Image("file:///" + item.getFile().getAbsolutePath()));
			currentSelectedImage = item;
			if(sendAction) {
				ImageAction action = new ImageAction(new FileKey(item.getFile().getName()));
				sendAction(PacketCode.IMAGE_ACTION, action);
			}
		}
	}

	private void localFileCheck(List<FileItem> items) {
		for (FileItem fileItem : items) {
			if (ClientStorageManager.getLocalFile(room, fileItem.getFile().getName()).exists()) {
				fileItem.isPhantomProperty().set(false);
			}
		}
	}

	@Override
	public void handleError(ErrorCode code) {
		if(code.equals(ErrorCode.REMOVE_DENIED)) {
			Dialogs.showAlert("다른사람의 파일은 방장이나 소유자만 삭제가능합니다");
		}
	}
	
}
