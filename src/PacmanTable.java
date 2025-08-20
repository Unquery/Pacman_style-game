import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class PacmanTable extends AbstractTableModel {


    public int[][]elems;
    PacmanTable(Integer columns, Integer rows){
        elems = new int[columns][rows];
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        elems[rowIndex][columnIndex] = (Integer) aValue;
        fireTableCellUpdated(rowIndex,columnIndex);
    }

    @Override
    public int getRowCount() {
        return elems.length;
    }

    @Override
    public int getColumnCount() {
        return elems[0].length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return elems[rowIndex][columnIndex];
    }

}
