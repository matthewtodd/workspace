package org.matthewtodd.intellij.rebalance;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splittable;
import com.intellij.openapi.ui.Splitter;
import java.awt.Component;
import javax.swing.JPanel;

public class RebalanceSplitters extends AnAction {
  @Override public void actionPerformed(AnActionEvent event) {
    Project project = event.getData(CommonDataKeys.PROJECT);
    JPanel topPanel = FileEditorManagerEx.getInstanceEx(project).getSplitters().getTopPanel();

    for (int i = 0; i < topPanel.getComponentCount(); i++) {
      Component component = topPanel.getComponent(i);
      if (component instanceof Splitter) {
        wrap((Splitter) component).rebalance();
      }
    }
  }

  private RebalancingSplitter wrap(Splitter splitter) {
    RebalancingSplitter.Builder builder = RebalancingSplitter.of(splitter);
    // HACK first and second component are JPanels.
    // We assume splitters will be in their 0th components if at all.
    // This will probably fail in the wild.
    if (splitter.getFirstComponent().getComponent(0) instanceof Splitter)
      builder.first(wrap((Splitter) splitter.getFirstComponent().getComponent(0)));
    if (splitter.getSecondComponent().getComponent(0) instanceof Splitter)
      builder.second(wrap((Splitter) splitter.getSecondComponent().getComponent(0)));
    return builder.build();
  }

  interface Rebalanceable {
    void rebalance();
    int count(boolean orientation);

    Rebalanceable NULL = new Rebalanceable() {
      @Override public void rebalance() {}
      @Override public int count(boolean orientation) {
        return 1;
      }
    };
  }

  // TODO name
  private static class RebalancingSplitter implements Rebalanceable {
    private final Splittable splittable;
    private final Rebalanceable first;
    private final Rebalanceable second;

    static Builder of(Splittable splittable) {
      return new Builder(splittable);
    }

    private RebalancingSplitter(Builder builder) {
      splittable = builder.splittable;
      first = builder.first;
      second = builder.second;
    }

    @Override public void rebalance() {
      int firstCount = first.count(splittable.getOrientation());
      int secondCount = second.count(splittable.getOrientation());
      System.out.printf("Rebalancing with %d, %d\n", firstCount, secondCount);
      splittable.setProportion(((float) firstCount) / (firstCount + secondCount));
      first.rebalance();
      second.rebalance();
    }

    // TODO better name. splits? panes?
    @Override public int count(boolean orientation) {
      if (orientation == splittable.getOrientation()) {
        return first.count(orientation) + second.count(orientation);
      } else {
        return Math.max(first.count(orientation), second.count(orientation));
      }
    }

    // TODO builder is too much ceremony here?
    static class Builder {
      private final Splittable splittable;
      private Rebalanceable first = Rebalanceable.NULL;
      private Rebalanceable second = Rebalanceable.NULL;

      Builder(Splittable splittable) {
        this.splittable = splittable;
      }

      Builder first(Rebalanceable first) {
        this.first = first;
        return this;
      }

      Builder second(Rebalanceable second) {
        this.second = second;
        return this;
      }

      RebalancingSplitter build() {
        return new RebalancingSplitter(this);
      }
    }
  }
}
