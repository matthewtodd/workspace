package org.matthewtodd.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.matthewtodd.console.ViewGroup.Orientation.COLUMNS;
import static org.matthewtodd.console.ViewGroup.Orientation.ROWS;

public class TableView extends ViewGroup {
  public TableView(String id) {
    super(id, COLUMNS);
  }

  public void table(Table table) {
    update(table.columnIds(), ColumnView::new, view -> view.column(table.column(view.id())));
  }

  private static class ColumnView extends ViewGroup {
    ColumnView(String id) {
      super(id, ROWS);
    }

    void column(Column column) {
      update(column.rowIds(), TextView::new, view -> view.text(column.row(view.id())));
    }
  }

  public interface Table {
    List<String> columnIds();
    Column column(String id);

    static Table fromMap(Map<String, Map<String, String>> map) {
      return new Table() {
        @Override public List<String> columnIds() {
          return new ArrayList<>(map.keySet());
        }

        @Override public Column column(String id) {
          return Column.fromMap(map.get(id));
        }
      };
    }
  }

  public interface Column {
    List<String> rowIds();
    String row(String id);

    static Column fromMap(Map<String, String> map) {
      return new Column() {
        @Override public List<String> rowIds() {
          return new ArrayList<>(map.keySet());
        }

        @Override public String row(String id) {
          return map.get(id);
        }
      };
    }
  }
}
