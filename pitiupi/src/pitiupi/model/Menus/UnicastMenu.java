package pitiupi.model.Menus;

import pitiupi.model.User;

public abstract class UnicastMenu extends Menu {
    public abstract void onClick(User user);
}
