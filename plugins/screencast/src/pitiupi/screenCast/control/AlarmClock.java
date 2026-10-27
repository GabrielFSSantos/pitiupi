package pitiupi.screenCast.control;

import java.util.TimerTask;
import pitiupi.screenCast.GUI.ScreenCastPanel;

public class AlarmClock extends TimerTask {

    private final ScreenCastPanel screenCastPanel;

    public AlarmClock(ScreenCastPanel screenCastPanel) {
        this.screenCastPanel = screenCastPanel;
    }

    @Override
    public void run() {
        screenCastPanel.sendScreenCastMessage();
    }
}
