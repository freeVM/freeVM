/*
 *  Copyright 2005 - 2006 The Apache Software Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 * @author Pavel Dolgov, Dmitry A. Durnev
 * @version $Revision$
 */
package java.awt;

import java.io.FilenameFilter;

public class FileDialog extends Dialog {
    private static final long serialVersionUID = 5035145889651310422L;

    public static final int LOAD = 0;

    public static final int SAVE = 1;

    private String file;
    private String directory;
    private int mode;
    private FilenameFilter filenameFilter;

    public FileDialog(Frame owner) {
        this(owner, "");
        toolkit.lockAWT();
        try {
        } finally {
            toolkit.unlockAWT();
        }
    }

    public FileDialog(Frame owner, String title) {
        this(owner, title, LOAD);
        toolkit.lockAWT();
        try {
        } finally {
            toolkit.unlockAWT();
        }
    }

    public FileDialog(Frame owner, String title, int mode) {
        super(owner, title, true); // initially always modal
        toolkit.lockAWT();
        try {
            setMode(mode);
            setLayout(null);
        } finally {
            toolkit.unlockAWT();
        }
    }
    
    public FileDialog(Dialog owner) {
        this(owner, "");
        toolkit.lockAWT();
        try {
        } finally {
            toolkit.unlockAWT();
        }
    }

    public FileDialog(Dialog owner, String title) {
        this(owner, title, LOAD);
        toolkit.lockAWT();
        try {
        } finally {
            toolkit.unlockAWT();
        }
    }

    public FileDialog(Dialog owner, String title, int mode) {
        super(owner, title, true); // initially always modal
        toolkit.lockAWT();
        try {
            setMode(mode);
            setLayout(null);
        } finally {
            toolkit.unlockAWT();
        }
    }

    public String getFile() {
        toolkit.lockAWT();
        try {
            return file;
        } finally {
            toolkit.unlockAWT();
        }
    }

    protected String paramString() {
        /* The format is based on 1.5 release behavior 
         * which can be revealed by the following code:
         * System.out.println(new FileDialog(new Frame()));
         */

        toolkit.lockAWT();
        try {
            return (super.paramString() + ",dir=" + directory +
                    ",file=" + file + "," +
                    (mode == LOAD ? "load" : "save"));
        } finally {
            toolkit.unlockAWT();
        }
    }

    public void addNotify() {
        toolkit.lockAWT();
        try {
            // TODO: implement
            super.addNotify();
        } finally {
            toolkit.unlockAWT();
        }
    }

    public String getDirectory() {
        toolkit.lockAWT();
        try {
            return directory;
        } finally {
            toolkit.unlockAWT();
        }
    }

    public FilenameFilter getFilenameFilter() {
        toolkit.lockAWT();
        try {
            return filenameFilter;
        } finally {
            toolkit.unlockAWT();
        }
    }

    public int getMode() {
        toolkit.lockAWT();
        try {
            return mode;
        } finally {
            toolkit.unlockAWT();
        }
    }

    public void setDirectory(String dir) {
        toolkit.lockAWT();
        try {
            directory = (dir == "" ? null : dir);
        } finally {
            toolkit.unlockAWT();
        }
    }

    public void setFile(String file) {
        toolkit.lockAWT();
        try {
            this.file = (file == "" ? null : file);
        } finally {
            toolkit.unlockAWT();
        }
    }

    public void setFilenameFilter(FilenameFilter filter) {
        toolkit.lockAWT();
        try {
            this.filenameFilter = filter;
        } finally {
            toolkit.unlockAWT();
        }
    }

    public void setMode(int mode) {
        toolkit.lockAWT();
        try {
            if (!((mode == LOAD) || (mode == SAVE))) {
                throw new IllegalArgumentException("illegal file dialog mode");
            }
            this.mode = mode;
        } finally {
            toolkit.unlockAWT();
        }
    }


    void showImpl() {
        if (toolkit.theme.showFileDialog(this)) {
            super.showImpl();
        }
    }

    void hideImpl() {
        if (toolkit.theme.hideFileDialog(this)) {        
            super.hideImpl();
        }
    }
    
    ComponentBehavior createBehavior() {
        return new HWBehavior(this) {
            public void removeNotify() {
                super.removeNotify();
                hideImpl();                
            }
        };
    }
    
    String autoName() {
        return "filedlg" + toolkit.autoNumber.nextFileDialog++;
    }
}

