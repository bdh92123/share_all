package polydopter.shareall.client.util;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;

public abstract class CustomListCell<T> extends ListCell<T> {
	
	protected Node node;
	protected String fxml;
	
	public CustomListCell(Node node) {
		this.node = node;
	}
	public CustomListCell(String fxml) {
		this.fxml = fxml;
	}
	
	@Override
	protected void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);
        if (empty || item == null)
        {
            setGraphic(null);
            return;
        }
        
        Node cell = null;
        if (this.node == null)
        {
        	if(fxml != null) {
        		cell = (Parent) FXUtil.loadFxml(fxml, this);	
        	} 
        	this.node = cell;
        }
        else
        {
            cell = this.node;
        }
        
        drawCell(item, cell);
        setGraphic(cell);
	}
	
	protected abstract void drawCell(T item, Node cell);
}
