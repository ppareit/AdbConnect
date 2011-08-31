package adbconnect.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import adbconnect.Activator;

public class Page extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    @Override
    protected void createFieldEditors() {
        addField(new StringFieldEditor(Activator.IP_ADDRESS_SETTING,
                "&IP address of device:",
                getFieldEditorParent()));
        addField(new StringFieldEditor(Activator.PORT_NUMBER_SETTING,
                "&Port number:",
                getFieldEditorParent()));
    }

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

}
