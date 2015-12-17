package polydopter.shareall.common.sync;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import polydopter.shareall.common.action.draw.DrawingAction;
import polydopter.shareall.common.action.draw.UndoAction;
import polydopter.shareall.common.data.User;

public class DrawingSync implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// <User session key, User's action list>
	private Map<User, List<DrawingAction>> actionMap;
	private int actionIndex = 0;
	
    
    public  Map<User, List<DrawingAction>> getActionMap() {
        return actionMap;
    }

    public DrawingSync() {
        setActionMap(new HashMap<>());
    }
    
    public  void applyAction(User user, DrawingAction action) {
        List<DrawingAction> userActionList = getActionMap().get(user);
        if(userActionList == null) {
            userActionList = new ArrayList<>();
            getActionMap().put(user, userActionList);
        }
        
        if(action instanceof UndoAction && userActionList.size() > 0) {
        	userActionList.remove(userActionList.size() - 1);
        } else {
        	userActionList.add(action);
        	action.index = actionIndex++;
        }
    }
    
    public  void removeUser(User user) {
        getActionMap().remove(user);
    }
    
    public  List<DrawingAction> getDrawingList() {
    	List<DrawingAction> allAction = new ArrayList<>();
    	for(List<DrawingAction> list : getActionMap().values()) {
    		allAction.addAll(list);
    	}
    	allAction.sort(new Comparator<DrawingAction>() {
    		@Override
    		public int compare(DrawingAction o1, DrawingAction o2) {
    			return Integer.compare(o1.index, o2.index);
    		}
		});
    	return allAction;
    }

	public void setActionMap(Map<User, List<DrawingAction>> actionMap) {
		this.actionMap = actionMap;
	}
}
