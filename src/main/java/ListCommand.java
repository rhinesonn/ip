import java.util.List;

/**
 * A command that displays every task in the current list.
 */
public final class ListCommand extends Command {
    @Override
    public void execute(List<Task> tasks, Ui ui) {
        ui.showTasks(tasks);
    }
}
