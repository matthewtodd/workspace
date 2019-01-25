package org.matthewtodd.perquackey.console;

import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.TextGUI;
import com.googlecode.lanterna.gui2.TextGUIThread;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.Terminal;
import java.io.IOException;
import java.util.function.Function;
import org.matthewtodd.flow.Flow;
import org.matthewtodd.workflow.Workflow;
import org.matthewtodd.workflow.WorkflowScreen;

class Application {
  private final Workflow<?, ?> workflow;
  private final Function<WorkflowScreen<?, ?>, Component> viewFactory;
  private final Terminal terminal;
  private final Flow.Scheduler scheduler;

  Application(Workflow<?, ?> workflow, Function<WorkflowScreen<?, ?>, Component> viewFactory,
      Terminal terminal, Flow.Scheduler scheduler) {
    this.workflow = workflow;
    this.viewFactory = viewFactory;
    this.terminal = terminal;
    this.scheduler = scheduler;
  }

  void run(Flow.Scheduler mainThread) {
    mainThread.accept(() -> start(mainThread::shutdown));
    mainThread.start();
  }

  void start(Runnable onComplete) {
    TerminalScreen screen;

    try {
      screen = new TerminalScreen(terminal);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    try {
      screen.startScreen();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    MultiWindowTextGUI gui = new MultiWindowTextGUI(textGUI -> new MyTextGUIThread(scheduler, textGUI), screen);
    Window window = new BasicWindow("Perquackey");
    gui.addWindow(window);

    Flow.of(workflow.screen())
        .as(viewFactory::apply)
        .subscribe(window::setComponent);

    Flow.of(workflow.result())
        .onComplete(onComplete)
        .subscribe(turn -> {
          try {
            screen.stopScreen();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });

    workflow.start(null);
  }

  // This isn't quite right, but the Rx scheduler idea is really close to the lanterna gui thread one.
  // I can imagine we'll combine terminal and scheduler up above and really just expose a window-like
  // Consumer<Component> here, something like that.
  private class MyTextGUIThread implements TextGUIThread {
    private final Flow.Scheduler scheduler;
    private final TextGUI textGUI;

    MyTextGUIThread(Flow.Scheduler scheduler, TextGUI textGUI) {
      this.scheduler = scheduler;
      this.textGUI = textGUI;
    }

    @Override public void invokeLater(Runnable runnable) throws IllegalStateException {
      scheduler.accept(runnable);
    }

    @Override public boolean processEventsAndUpdate() throws IOException {
      textGUI.processInput();

      if (textGUI.isPendingUpdate()) {
        textGUI.updateScreen();
        return true;
      }

      return false;
    }

    @Override public void invokeAndWait(Runnable runnable)
        throws IllegalStateException, InterruptedException {
      invokeLater(runnable);
    }

    @Override public void setExceptionHandler(ExceptionHandler exceptionHandler) {

    }

    @Override public Thread getThread() {
      return null;
    }
  }
}
