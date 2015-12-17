package polydopter.shareall.client.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Callback;
import polydopter.shareall.client.ui.base.BaseController;


public class FXUtil {
	
    private static final ObservableList<?> emptyList = FXCollections.observableArrayList();
    private static ClassLoader cachingClassLoader = new CachingClassLoader(FXMLLoader.getDefaultClassLoader());
	
	@SuppressWarnings("unchecked")
	public static <T> T lookup(Node parent, String selector, Class<T> clazz) {
	    for (Node node : parent.lookupAll(selector)) {
	        if (node.getClass().isAssignableFrom(clazz)) {
	            return (T) node;
	        }
	    }
	    return null;
	}
	
	public static BaseController getController(Stage stage)
	{
	    try
        {
	    	Object data = stage.getScene().getRoot().getUserData();
	    	if(data instanceof BaseController) {
	    		return (BaseController) data;	
	    	}
        }catch(Exception e)
        {
        }
	    
	    return null;
	}
	
	public static void setMaxLength(final TextField field, final int maxlength)
    {
        field.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue) {
                if (field.getText().length() > maxlength) {
                    String s = field.getText().substring(0, maxlength);
                    field.setText(s);
                }
            }
        });
    }
	
	public static Color intToColor(int color)
	{
		int b = (color)&0xFF;
		int g = (color>>8)&0xFF;
		int r = (color>>16)&0xFF;
		float a = (color>>24)&0xFF / 255;
		return Color.rgb(r, g, b, a);
	}
	
	public static int colorToInt(Color color)
	{
		int b = (int)(color.getBlue()*255);
		int g = (int)(color.getGreen()*255);
		int r = (int)(color.getRed()*255);
		int a = (int)(color.getOpacity()*255);
		return (a<<24)|(r<<16)|(g<<8)|b;
	}
	
	public static String intToWeb(int color)
	{
		int b = (color)&0xFF;
		int g = (color>>8)&0xFF;
		int r = (color>>16)&0xFF;
		return hex2d(Integer.toString(r, 16)) + hex2d(Integer.toString(g, 16)) + hex2d(Integer.toString(b, 16));
	}
	
	public static String hex2d(String tmp)
	{
		if(tmp.length() == 1) return "0" + tmp;
		else return tmp;
	}
	
    public static void setMoverForWindow(final Node node, final Window window)
	{
	    EventHandler<MouseEvent> handler = new EventHandler<MouseEvent>() {
            double xOffset;
            double yOffset;
            
            @Override
            public void handle(MouseEvent event) {
                if(event.getEventType() == MouseEvent.MOUSE_PRESSED)
                {
                    xOffset = event.getSceneX();
                    yOffset = event.getSceneY();    
                }
                else if(event.getEventType() == MouseEvent.MOUSE_DRAGGED)
                {
                    if(window instanceof Window)
                    {
                        ((Window) window).setX(event.getScreenX() - xOffset);
                    }
                    
                    window.setX(event.getScreenX() - xOffset);
                    window.setY(event.getScreenY() - yOffset);    
                }
            }
        };
        if(node != null)
        {
        	node.addEventHandler(MouseEvent.MOUSE_PRESSED, handler);
            node.addEventHandler(MouseEvent.MOUSE_DRAGGED, handler);	
        }
        else
        {
        	window.addEventHandler(MouseEvent.MOUSE_PRESSED, handler);
            window.addEventHandler(MouseEvent.MOUSE_DRAGGED, handler);
        }
        
	}
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static void refreshListView(ListView listView)
    {
        ObservableList list = listView.getItems();
        listView.setItems(emptyList);
        listView.setItems(list);
    }
    
    public static void hide(Region region)
    {
    	region.setVisible(false);
    	region.setMinHeight(0);
    	region.setMaxHeight(0);
    }
    
    public static void remove(Region region)
    {
    	if(region.getParent() == null) return;
    	((Pane)region.getParent()).getChildren().remove(region);
    }
    
    public static void show(Region region)
    {
    	region.setVisible(true);
    	region.setMinHeight(Region.USE_COMPUTED_SIZE);
    	region.setMaxHeight(Region.USE_COMPUTED_SIZE);
    }
    
    public static <T> void setCellFactory(ComboBox<T> comboBox, Class<? extends ListCell<T>> listCellClazz, Object enclosingObject)
    {
    	comboBox.setCellFactory(
    	new Callback<ListView<T>, javafx.scene.control.ListCell<T>>()
        {
            @Override
            public ListCell<T> call(ListView<T> listView)
            {
                BorderPane cellRoot = new BorderPane();
                try {
                	Constructor<? extends ListCell<T>> con = null;
                	
                	if(enclosingObject != null)
                	{
                		con = listCellClazz.getConstructor(enclosingObject.getClass());
                	}
                	
                	ListCell<T> cell = (ListCell<T>) (enclosingObject != null && con != null ? con.newInstance(enclosingObject) : listCellClazz.newInstance());
                	
                	cell.itemProperty().addListener((obs, oldItem, newItem) -> {
                        if (newItem != null)
                        {
                        }
                    });
                    cell.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> {
                        if (isEmpty)
                        {
                            cell.setGraphic(null);
                        }
                        else
                        {
                            cell.setGraphic(cellRoot);
                        }
                    });
                    cell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    return cell;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
            }

        });
    }
    
    public static <T> void setCellFactory(ListView<T> listView, Class<? extends ListCell<T>> listCellClazz, Object enclosingObject)
    {
    	listView.setCellFactory(
    	new Callback<ListView<T>, javafx.scene.control.ListCell<T>>()
        {
            @Override
            public ListCell<T> call(ListView<T> listView)
            {
                BorderPane cellRoot = new BorderPane();
                try {
                	Constructor<? extends ListCell<T>> con = null;
                	
                	if(enclosingObject != null)
                	{
                		con = listCellClazz.getDeclaredConstructor(enclosingObject.getClass());
                	}
                	
                	ListCell<T> cell = (ListCell<T>) (enclosingObject != null && con != null ? con.newInstance(enclosingObject) : listCellClazz.newInstance());
                	
                	cell.itemProperty().addListener((obs, oldItem, newItem) -> {
                        if (newItem != null)
                        {
                        }
                    });
                    cell.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> {
                        if (isEmpty)
                        {
                            cell.setGraphic(null);
                        }
                        else
                        {
                            cell.setGraphic(cellRoot);
                        }
                    });
                    cell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    return cell;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
            }

        });
    }

    public static void setAnchor(Node node, Double value)
    {
    	AnchorPane.setBottomAnchor(node, value);
    	AnchorPane.setLeftAnchor(node, value);
    	AnchorPane.setRightAnchor(node, value);
    	AnchorPane.setTopAnchor(node, value);
    }
    
    public static String streamToString(InputStream is)
    {
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	
    	try {
        	byte[] b = new byte[128];
        	int read = -1;
        	while((read = is.read(b)) > 0)
        	{
        		bos.write(b, 0, read);
        	}	
		} catch (Exception e) {
		} finally {
			try {
				is.close();	
			} catch (Exception e2) {
			}
		}
    	
    	return bos.toString();
    }
    
    public static String baseCss()
    {
    	return "css/base.css";
    }
    
    public static void setIcon(Stage stage)
    {
    	try {
    		InputStream iconIs = null;
			if(stage.getClass().getClassLoader().getResourceAsStream("ShareAll.png") != null)
				iconIs = stage.getClass().getClassLoader().getResourceAsStream("ShareAll.png");
			if(iconIs == null)
				iconIs = new FileInputStream("ShareAll.png");
			if(iconIs != null)
			{
				stage.getIcons().clear();
				stage.getIcons().add(new Image(iconIs));
			}	
		} catch (Exception e) {
		}
    }
    
    public static Node loadFxml(String fxml) 
    {
    	return loadFxml(fxml, null);
    }
    		
    public static Node loadFxml(String fxml, Object controller)
    {
    	try {
    		FXMLLoader loader = new FXMLLoader();
        	loader.setClassLoader(cachingClassLoader);
        	loader.setController(controller);
        	URL url = new URL(null, "cp:", new CpURLStreamHandler());
        	loader.setLocation(url);
        	return loader.load(getFXMLInputStream(fxml));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }
    
    public static InputStream getFXMLInputStream(String fxml) throws IOException
	{
		return FXUtil.class.getClassLoader().getResourceAsStream("fxml/" + fxml);
	}
	
    public static Stage initStage(String fxml, Object controller)
    {
    	return initStage(fxml, controller, null);
    }
    
    public static Stage initStage(String fxml, Object controller, AtomicReference<Node> root)
    {
    	try {
    		Stage stage = new Stage();
        	FXMLLoader loader = new FXMLLoader();
        	loader.setClassLoader(cachingClassLoader);
    		loader.setController(controller);
    		Pane parent = (Pane) loader.load(getFXMLInputStream(fxml));
    		stage.centerOnScreen();
    		stage.initStyle(StageStyle.UNDECORATED);
    		stage.initStyle(StageStyle.TRANSPARENT);
    		Pane pane = new Pane();
    		pane.getChildren().add(parent);
    		pane.setStyle("-fx-background-color: transparent");
    		if(root != null)
    			root.set(parent);
    		Scene scene = new Scene(pane);
    		scene.setFill(null);
    		scene.getStylesheets().add(FXUtil.baseCss());
    		scene.getStylesheets().add(FXUtil.fontCss());
    	    stage.setScene(scene);
    	    return stage;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }

	private static String fontCss() {
		return "css/font.css";
	}

	public static Image getImage(String path) {
		return new Image(FXUtil.class.getResourceAsStream(path));
	}
	
	public static String getIdentity()
	{
		try {
			InetAddress ip = InetAddress.getLocalHost();
			// 네트워크 인터페이스 취득
			NetworkInterface netif = NetworkInterface.getByInetAddress(ip);

			// 네트워크 인터페이스가 NULL이 아니면
			if (netif != null) {
				// 맥어드레스 취득
				byte[] mac = netif.getHardwareAddress();
				
				String macStr = "";
				// 맥어드레스 출력
				for (byte b : mac) {
					macStr = macStr + String.format("%02X:", b);
				}
				
				return macStr.substring(0, macStr.length()-1);
			}
		} catch (Exception e) {
			
		}
		return null;
	}
}
