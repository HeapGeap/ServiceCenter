import javax.swing.text.View;

class ComboItem  {
    private final String key;
    private final String value;

    public ComboItem(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return key;
    }

    public String getValue() {
        return value;
    }

}

@Override
public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    Menuinflater inflater = getMenuinflater();
    inflater.inflate(R.rnenu.context_rnenu, menu);
}
