/*******************************************************************************
 * Copyright (c) 2011 Pieter Pareit.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Pieter Pareit - initial API and implementation
 ******************************************************************************/
package adbconnect.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import adbconnect.Activator;


/**
 * Handles the executing of the command, and the updating of the button.
 */
public class ConnectHandler extends AbstractHandler implements IElementUpdater {

    public ConnectHandler() {
    }

    public Object execute(ExecutionEvent event) throws ExecutionException {
        Job job = new Job("Connect to adb over wifi") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    String deviceIpAddress = Activator.getDeviceIpAddress();
                    String devicePortNumber = Activator.getDevicePortNumber();
                    new ProcessBuilder("adb", "connect", deviceIpAddress + ":" + devicePortNumber)
                            .start();
                } catch (IOException e) {
                    String message = "Adb command not found. Is the android SDK installed? Is 'adb' in the path?";
                    error(message);
                    return new Status(Status.ERROR, Activator.PLUGIN_ID,
                            message);
                }
                return Status.OK_STATUS;
            }
        };
        job.schedule();
        return null;
    }

    private void error(String message) {
        ILog log = Activator.getDefault().getLog();
        String pluginId = Activator.PLUGIN_ID;
        log.log(new Status(Status.ERROR, pluginId, message));
        System.out.println(message);
    }

    private void log(String message) {
        ILog log = Activator.getDefault().getLog();
        String pluginId = Activator.PLUGIN_ID;
        log.log(new Status(Status.INFO, pluginId, message));
        System.out.println(message);
    }

    /* 
     * Updates the button. If there is a connection to an android device
     * the on icon is shown, otherwise the off icon is shown. This uses polling.
     * 
     * TODO: We need a way to start this when eclipse starts.
     * 
     * @see org.eclipse.ui.commands.IElementUpdater#updateElement(org.eclipse.ui.menus.UIElement, java.util.Map)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void updateElement(final UIElement element, Map parameters) {
        final ImageDescriptor onIcon = Activator.getImageDescriptor("icons/icon.png");
        final ImageDescriptor offIcon = Activator.getImageDescriptor("icons/icon_off.png");

        Job job = new Job("Update Adb Connect toolbar icon") {
            boolean loop = true;
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                loop = true;
                while (loop) {
                    try {
                        Display.getDefault().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                boolean on;
                                try {
                                    on = isConnected();
                                } catch (AdbNotInstalledException e) {
                                    loop = false;
                                    on = false;
                                }
                                element.setIcon(on ? onIcon : offIcon);
                            }
                        });
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return Status.CANCEL_STATUS;
            }
        };
        job.setSystem(true);
        job.setUser(false);
        job.schedule();
    }

    class AdbNotInstalledException extends Exception {
        private static final long serialVersionUID = -5248866829075299316L;
    }

    /**
     * @return true if there is a connection over wifi to an android device
     * @throws AdbNotInstalledException 
     */
    private boolean isConnected() throws AdbNotInstalledException {
        try {
            Process p = new ProcessBuilder("adb", "devices").start();
            p.waitFor();
            InputStream is = p.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line =  br.readLine();
            if (!line.equals("List of devices attached ")) {
                log("Unexpected output from 'adb devices': " + line);
            }
            while ((line = br.readLine()) != null) {
                String [] ss = line.split("\\s");
                if (ss.length != 2) continue;
                String [] ss0 = ss[0].split(":");
                if (ss0.length != 2) continue;
                String adress = ss0[0];
                String port = ss0[1];
                String device = ss[1];
                if (adress.equals(Activator.getDeviceIpAddress()) &&
                        port.equals(Activator.getDevicePortNumber()) &&
                        device.equals("device")) {
                    return true;
                }
            }
        } catch (IOException e) {
            // is thrown by ProcessBuilder.start() when adb is not installed, or not in path
            throw new AdbNotInstalledException();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}



























