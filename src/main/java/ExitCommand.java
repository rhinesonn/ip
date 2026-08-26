import java.util.List;

/**
 * A command that ends the application.
 */
public final class ExitCommand extends Command {
    @Override
    public void execute(List<Task> tasks, Ui ui) {
        ui.showFarewell();
    }

    @Override
    public boolean isExit() {
        return true;
    }
}
