import java.util.List;

/**
 * A command representing input that Caitlyn does not recognize.
 */
public final class UnknownCommand extends Command {
    @Override
    public void execute(List<Task> tasks, Ui ui) throws CaitlynException {
        throw new CaitlynException(
                "I humbly beg your pardon, master. I do not know how to carry out that command.");
    }
}
