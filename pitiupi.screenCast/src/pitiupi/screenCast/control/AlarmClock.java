/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pitiupi.screenCast.control;

import java.util.TimerTask;
import pitiupi.screenCast.GUI.ScreenCastWindow;

/**
 *
 * @author flavio
 */
public class AlarmClock extends TimerTask {

    private final ScreenCastWindow screenCastWindow;
    
    public AlarmClock(ScreenCastWindow mainWindow) {
        this.screenCastWindow = mainWindow;
    }

    @Override
    public void run() {
        this.screenCastWindow.sendScreenCastMessage();
    }
}
