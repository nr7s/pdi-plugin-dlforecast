/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
*
*******************************************************************************
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
******************************************************************************/

package org.pentaho.di.ui.jobentry.deepforecast;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.jobentry.deepforecast.DeepForecastJobEntry;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


public class DeepForecastJobEntryDialog extends JobEntryDialog implements JobEntryDialogInterface {
    private static Class<?> PKG = DeepForecastJobEntry.class; // for i18n purposes

    private static final String[] FILETYPES = new String[] { BaseMessages.getString(PKG, "DeepForecast.Filetype.All")};

    private Label wlName;
    private Text wName;
    private FormData fdlName, fdName;

    private Label wlFilename;
    private Button wbFilename;
    private TextVar wFilename;
    private FormData fdlFilename, fdbFilename, fdFilename;

    private Label wlOutputFolder;
    private Button wbOutputFolder;
    private TextVar wOutputFolder;
    private FormData fdlOutputFolder, fdbOutputFolder, fdOutputFolder;

    private Label wlTempDir;
    private Button wbTempDir;
    private TextVar wTempDir;
    private FormData fdlTempDir, fdbTempDir, fdTempDir;

    private Label wlForecastSteps;
    private TextVar wForecastSteps;
    private FormData fdlForecastSteps, fdForecastSteps;

    private Label wlConfigPath;
    private Button wbConfigPath;
    private TextVar wConfigPath;
    private FormData fdlConfigPath, fdbConfigPath, fdConfigPath;

    private Label wlTarget;
    private TextVar wTarget;
    private FormData fdlTarget, fdTarget;

    private Label wlModelName;
    private TextVar wModelName;
    private FormData fdlModelName, fdModelName;

    private Label wlToLoadFile;
    private Button wbToLoadFile;
    private TextVar wToLoadFile;
    private FormData fdlToLoadFile, fdbToLoadFile, fdToLoadFile;

    private Label wlRunConfiguration;
    private CCombo wRunConfiguration;
    private FormData fdlRunConfiguration, fdRunConfiguration;


    private Button wOK, wCancel;
    private Listener lsOK, lsCancel;
    private SelectionAdapter lsDef;

    private DeepForecastJobEntry meta;
    private Shell shell;

    private boolean changed;

    public DeepForecastJobEntryDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta ) {
        super( parent, jobEntryInt, rep, jobMeta );
        meta = (DeepForecastJobEntry) jobEntryInt;
        if ( this.meta.getName() == null ) {
            this.meta.setName( BaseMessages.getString( PKG, "DeepForecast.Default.Name") );
        }
    }

    public JobEntryInterface open() {
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
        props.setLook( shell );
        JobDialog.setShellImage( shell, meta );

        ModifyListener lsMod = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                meta.setChanged();
            }
        };
        changed = meta.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout( formLayout );
        shell.setText( BaseMessages.getString(PKG, "DeepForecast.Shell.Title")  );

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Job entry name line
        wlName = new Label( shell, SWT.RIGHT );
        wlName.setText( BaseMessages.getString( PKG, "DeepForecast.JobEntryName.Label" ) );
        props.setLook( wlName );
        fdlName = new FormData();
        fdlName.left = new FormAttachment( 0, 0 );
        fdlName.right = new FormAttachment( middle, -margin );
        fdlName.top = new FormAttachment( 0, margin );
        wlName.setLayoutData( fdlName );
        wName = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook( wName );
        wName.setToolTipText( BaseMessages.getString( PKG, "DeepForecast.JobEntryName.Tooltip" ) );
        wName.addModifyListener( lsMod );
        fdName = new FormData();
        fdName.left = new FormAttachment( middle, 0 );
        fdName.top = new FormAttachment( 0, margin );
        fdName.right = new FormAttachment( 100, 0 );
        wName.setLayoutData( fdName );

        // Filename line
        wlFilename = new Label( shell, SWT.RIGHT );
        wlFilename.setText( BaseMessages.getString( PKG, "DeepForecast.Filename.Label" ) );
        props.setLook( wlFilename );
        fdlFilename = new FormData();
        fdlFilename.left = new FormAttachment( 0, 0 );
        fdlFilename.top = new FormAttachment( wName, margin );
        fdlFilename.right = new FormAttachment( middle, -margin );
        wlFilename.setLayoutData( fdlFilename );

        wbFilename = new Button(shell, SWT.PUSH | SWT.CENTER);
        props.setLook( wbFilename );
        wbFilename.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
        fdbFilename = new FormData();
        fdbFilename.right = new FormAttachment( 100, 0 );
        fdbFilename.top = new FormAttachment( wName, 0 );
        wbFilename.setLayoutData( fdbFilename );

        wFilename = new TextVar( jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook( wFilename );
        wFilename.addModifyListener( lsMod );
        fdFilename = new FormData();
        fdFilename.left = new FormAttachment( middle, 0 );
        fdFilename.top = new FormAttachment( wName, margin );
        fdFilename.right = new FormAttachment( wbFilename, -margin );
        wFilename.setLayoutData( fdFilename );

        // Whenever something changes, set the tooltip to the expanded version:
        wFilename.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent e ) {
                wFilename.setToolTipText( BaseMessages.getString( PKG, "DeepForecast.Filename.Tooltip" ) );
            }
        } );

        wbFilename.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                FileDialog dialog = new FileDialog( shell, SWT.SAVE );
                dialog.setFilterExtensions( new String[] { "*" } );
                if ( wFilename.getText() != null ) {
                    dialog.setFileName( jobMeta.environmentSubstitute( wFilename.getText() ) );
                }
                dialog.setFilterNames( FILETYPES );
                if ( dialog.open() != null ) {
                    wFilename.setText( dialog.getFilterPath() + Const.FILE_SEPARATOR + dialog.getFileName() );
                }
            }
        } );

        // output folder
        wlOutputFolder = new Label( shell, SWT.RIGHT );
        wlOutputFolder.setText( BaseMessages.getString( PKG, "DeepForecast.OutputFolder.Label" ) );
        props.setLook( wlOutputFolder );
        fdlOutputFolder = new FormData();
        fdlOutputFolder.left = new FormAttachment( 0, 0 );
        fdlOutputFolder.top = new FormAttachment( wFilename, 2 * margin );
        fdlOutputFolder.right = new FormAttachment( middle, -margin );
        wlOutputFolder.setLayoutData( fdlOutputFolder );
        wbOutputFolder = new Button( shell, SWT.PUSH | SWT.CENTER );
        props.setLook( wbOutputFolder );
        wbOutputFolder.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
        fdbOutputFolder = new FormData();
        fdbOutputFolder.right = new FormAttachment( 100, -margin );
        fdbOutputFolder.top = new FormAttachment( wFilename, 2 * margin );
        wbOutputFolder.setLayoutData( fdbOutputFolder );
        wbOutputFolder.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                DirectoryDialog ddialog = new DirectoryDialog( shell, SWT.OPEN );
                if ( wOutputFolder.getText() != null ) {
                    ddialog.setFilterPath( jobMeta.environmentSubstitute( wOutputFolder.getText() ) );
                }
                // Calling open() will open and run the dialog.
                // It will return the selected directory, or
                // null if user cancels
                String dir = ddialog.open();
                if ( dir != null ) {
                    // Set the text box to the new selection
                    wOutputFolder.setText( dir );
                }

            }
        } );
        wOutputFolder = new TextVar( jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook( wOutputFolder );
        wOutputFolder.setToolTipText( BaseMessages.getString( PKG, "DeepForecast.OutputFolder.Tooltip" ) );
        wOutputFolder.addModifyListener( lsMod );
        fdOutputFolder = new FormData();
        fdOutputFolder.left = new FormAttachment( middle, 0 );
        fdOutputFolder.top = new FormAttachment( wFilename, 2 * margin );
        fdOutputFolder.right = new FormAttachment( wbOutputFolder, -margin );
        wOutputFolder.setLayoutData( fdOutputFolder );

        // temporary directory
        wlTempDir = new Label( shell, SWT.RIGHT );
        wlTempDir.setText( BaseMessages.getString( PKG, "DeepForecast.TempDir.Label" ) );
        props.setLook( wlTempDir );
        fdlTempDir = new FormData();
        fdlTempDir.left = new FormAttachment( 0, 0 );
        fdlTempDir.top = new FormAttachment( wOutputFolder, 2 * margin );
        fdlTempDir.right = new FormAttachment( middle, -margin );
        wlTempDir.setLayoutData( fdlTempDir );
        wbTempDir = new Button( shell, SWT.PUSH | SWT.CENTER );
        props.setLook( wbTempDir );
        wbTempDir.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
        fdbTempDir = new FormData();
        fdbTempDir.right = new FormAttachment( 100, -margin );
        fdbTempDir.top = new FormAttachment( wOutputFolder, 2 * margin );
        wbTempDir.setLayoutData( fdbTempDir );
        wbTempDir.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                DirectoryDialog ddialog = new DirectoryDialog( shell, SWT.OPEN );
                if ( wTempDir.getText() != null ) {
                    ddialog.setFilterPath( jobMeta.environmentSubstitute( wTempDir.getText() ) );
                }
                // Calling open() will open and run the dialog.
                // It will return the selected directory, or
                // null if user cancels
                String dir = ddialog.open();
                if ( dir != null ) {
                    // Set the text box to the new selection
                    wTempDir.setText( dir );
                }

            }
        } );
        wTempDir = new TextVar( jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook( wTempDir );
        wTempDir.setToolTipText( BaseMessages.getString( PKG, "DeepForecast.TempDir.Tooltip" ) );
        wTempDir.addModifyListener( lsMod );
        fdTempDir = new FormData();
        fdTempDir.left = new FormAttachment( middle, 0 );
        fdTempDir.top = new FormAttachment( wOutputFolder, 2 * margin );
        fdTempDir.right = new FormAttachment( wbTempDir, -margin );
        wTempDir.setLayoutData( fdTempDir );

        // Forecast Steps line
        wlForecastSteps = new Label( shell, SWT.RIGHT );
        wlForecastSteps.setText( BaseMessages.getString( PKG, "DeepForecast.ForecastSteps.Label" ) );
        props.setLook( wlForecastSteps );
        fdlForecastSteps = new FormData();
        fdlForecastSteps.left = new FormAttachment( 0, 0 );
        fdlForecastSteps.top = new FormAttachment( wTempDir, margin );
        fdlForecastSteps.right = new FormAttachment( middle, -margin );
        wlForecastSteps.setLayoutData( fdlForecastSteps );
        wForecastSteps = new TextVar( jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook( wForecastSteps );
        wForecastSteps.setToolTipText( BaseMessages.getString( PKG, "DeepForecast.ForecastSteps.Tooltip" ) );
        wForecastSteps.addModifyListener( lsMod );
        fdForecastSteps = new FormData();
        fdForecastSteps.left = new FormAttachment( middle, 0 );
        fdForecastSteps.top = new FormAttachment( wTempDir, margin );
        fdForecastSteps.right = new FormAttachment( 100, 0 );
        wForecastSteps.setLayoutData( fdForecastSteps );

        // Config file path line
        wlConfigPath = new Label( shell, SWT.RIGHT );
        wlConfigPath.setText( BaseMessages.getString( PKG, "DeepForecast.ConfigPath.Label" ) );
        props.setLook( wlConfigPath );
        fdlConfigPath = new FormData();
        fdlConfigPath.left = new FormAttachment( 0, 0 );
        fdlConfigPath.top = new FormAttachment( wForecastSteps, margin );
        fdlConfigPath.right = new FormAttachment( middle, -margin );
        wlConfigPath.setLayoutData( fdlConfigPath );
        wbConfigPath = new Button(shell, SWT.PUSH | SWT.CENTER);
        props.setLook( wbConfigPath );
        wbConfigPath.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
        fdbConfigPath = new FormData();
        fdbConfigPath.right = new FormAttachment( 100, 0 );
        fdbConfigPath.top = new FormAttachment( wForecastSteps, 0 );
        wbConfigPath.setLayoutData( fdbConfigPath );
        wConfigPath = new TextVar( jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook( wConfigPath );
        wConfigPath.addModifyListener( lsMod );
        fdConfigPath = new FormData();
        fdConfigPath.left = new FormAttachment( middle, 0 );
        fdConfigPath.top = new FormAttachment( wForecastSteps, margin );
        fdConfigPath.right = new FormAttachment( wbConfigPath, 0 );
        wConfigPath.setLayoutData( fdConfigPath );

        wConfigPath.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent e ) {
                wConfigPath.setToolTipText( BaseMessages.getString( PKG, "DeepForecast.ConfigPath.Tooltip" ) );
            }
        } );

        wbConfigPath.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                FileDialog dialog = new FileDialog( shell, SWT.SAVE );
                dialog.setFilterExtensions( new String[] { "*" } );
                if ( wConfigPath.getText() != null ) {
                    dialog.setFileName( jobMeta.environmentSubstitute( wConfigPath.getText() ) );
                }
                dialog.setFilterNames( FILETYPES );
                if ( dialog.open() != null ) {
                    wConfigPath.setText( dialog.getFilterPath() + Const.FILE_SEPARATOR + dialog.getFileName() );
                }
            }
        } );

        // target column line
        wlTarget = new Label( shell, SWT.RIGHT );
        wlTarget.setText( BaseMessages.getString( PKG, "DeepForecast.Target.Label" ) );
        props.setLook( wlTarget );
        fdlTarget = new FormData();
        fdlTarget.left = new FormAttachment( 0, 0 );
        fdlTarget.top = new FormAttachment( wConfigPath, margin );
        fdlTarget.right = new FormAttachment( middle, -margin );
        wlTarget.setLayoutData( fdlTarget );
        wTarget = new TextVar( jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook( wTarget );
        wTarget.setToolTipText( BaseMessages.getString( PKG, "DeepForecast.Target.Tooltip" ) );
        wTarget.addModifyListener( lsMod );
        fdTarget = new FormData();
        fdTarget.left = new FormAttachment( middle, 0 );
        fdTarget.top = new FormAttachment( wConfigPath, margin );
        fdTarget.right = new FormAttachment( 100, 0 );
        wTarget.setLayoutData( fdTarget );

        // Model name line
        wlModelName = new Label( shell, SWT.RIGHT );
        wlModelName.setText( BaseMessages.getString( PKG, "DeepForecast.ModelName.Label" ) );
        props.setLook( wlModelName );
        fdlModelName = new FormData();
        fdlModelName.left = new FormAttachment( 0, 0 );
        fdlModelName.top = new FormAttachment( wTarget, margin );
        fdlModelName.right = new FormAttachment( middle, -margin );
        wlModelName.setLayoutData( fdlModelName );
        wModelName = new TextVar( jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook( wModelName );
        wModelName.setToolTipText( BaseMessages.getString( PKG, "DeepForecast.ModelName.Tooltip" ) );
        wModelName.addModifyListener( lsMod );
        fdModelName = new FormData();
        fdModelName.left = new FormAttachment( middle, 0 );
        fdModelName.top = new FormAttachment( wTarget, margin );
        fdModelName.right = new FormAttachment( 100, 0 );
        wModelName.setLayoutData( fdModelName );

        // to load file
        wlToLoadFile = new Label( shell, SWT.RIGHT );
        wlToLoadFile.setText( BaseMessages.getString( PKG, "DeepForecast.ToLoadFile.Label" ) );
        props.setLook( wlToLoadFile );
        fdlToLoadFile = new FormData();
        fdlToLoadFile.left = new FormAttachment( 0, 0 );
        fdlToLoadFile.top = new FormAttachment( wModelName, margin );
        fdlToLoadFile.right = new FormAttachment( middle, 0 );
        wlToLoadFile.setLayoutData( fdlToLoadFile );

        wbToLoadFile = new Button( shell, SWT.PUSH | SWT.CENTER );
        props.setLook( wbToLoadFile );
        wbToLoadFile.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
        fdbToLoadFile = new FormData();
        fdbToLoadFile.top = new FormAttachment( wModelName, margin );
        fdbToLoadFile.right = new FormAttachment( 100, 0 );
        wbToLoadFile.setLayoutData( fdbToLoadFile );

        wToLoadFile = new TextVar( jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook( wToLoadFile );
        wToLoadFile.addModifyListener( lsMod );
        fdToLoadFile = new FormData();
        fdToLoadFile.left = new FormAttachment( middle, 0 );
        fdToLoadFile.right = new FormAttachment( wbToLoadFile, -margin );
        fdToLoadFile.top = new FormAttachment( wModelName, margin );
        wToLoadFile.setLayoutData( fdToLoadFile );

        wToLoadFile.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent e ) {
                wToLoadFile.setToolTipText( BaseMessages.getString( PKG, "DeepForecast.ToLoadFile.Tooltip" ) );
            }
        } );

        wbToLoadFile.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                FileDialog dialog = new FileDialog( shell, SWT.SAVE );
                dialog.setFilterExtensions( new String[] { "*" } );
                if ( wToLoadFile.getText() != null ) {
                    dialog.setFileName( jobMeta.environmentSubstitute( wToLoadFile.getText() ) );
                }
                dialog.setFilterNames( FILETYPES );
                if ( dialog.open() != null ) {
                    wToLoadFile.setText( dialog.getFilterPath() + Const.FILE_SEPARATOR + dialog.getFileName() );
                }
            }
        } );

        // Run configuration
        wlRunConfiguration = new Label( shell, SWT.RIGHT );
        wlRunConfiguration.setText( BaseMessages.getString( PKG, "DeepForecast.RunConfiguration.Label" ) );
        props.setLook( wlRunConfiguration );
        fdlRunConfiguration = new FormData();
        fdlRunConfiguration.left = new FormAttachment( 0, 0 );
        fdlRunConfiguration.right = new FormAttachment( middle, -margin );
        fdlRunConfiguration.top = new FormAttachment( wToLoadFile, margin );
        wlRunConfiguration.setLayoutData( fdlRunConfiguration );
        wRunConfiguration = new CCombo( shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
        wRunConfiguration.setItems( meta.runConfigurationOptions );
        wRunConfiguration.select( 0 ); // +1: starts at -1
        props.setLook( wRunConfiguration );

        fdRunConfiguration = new FormData();
        fdRunConfiguration.left = new FormAttachment( middle, 0 );
        fdRunConfiguration.top = new FormAttachment( wToLoadFile, margin );
        fdRunConfiguration.right = new FormAttachment( 100, 0 );
        wRunConfiguration.setLayoutData( fdRunConfiguration );

        wRunConfiguration.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {

            }
        } );

        wOK = new Button( shell, SWT.PUSH );
        wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
        wCancel = new Button( shell, SWT.PUSH );
        wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
        BaseStepDialog.positionBottomButtons( wRunConfiguration, new Button[] { wOK, wCancel }, margin, null );

        lsCancel = new Listener() {
            public void handleEvent( Event e ) {
                cancel();
            }
        };

        lsOK = new Listener() {
            public void handleEvent( Event e ) {
                ok();
            }
        };

        wCancel.addListener( SWT.Selection, lsCancel );
        wOK.addListener( SWT.Selection, lsOK );

        // Default listener when hitting enter
        lsDef = new SelectionAdapter() {
            public void widgetDefaultSelected( SelectionEvent e ) {
                ok();
            }
        };

        wName.addSelectionListener( lsDef );
        wFilename.addSelectionListener( lsDef );
        wOutputFolder.addSelectionListener( lsDef );
        wForecastSteps.addSelectionListener( lsDef );
        wConfigPath.addSelectionListener( lsDef );
        wModelName.addSelectionListener( lsDef );
        wToLoadFile.addSelectionListener( lsDef );
        wTarget.addSelectionListener( lsDef );

        // Detect X or ALT-F4 or something that kills this window and cancel the dialog properly
        shell.addShellListener( new ShellAdapter() {
            public void shellClosed( ShellEvent e ) {
                cancel();
            }
        } );

        populateDialog();

        BaseStepDialog.setSize( shell );

        shell.open();
        while ( !shell.isDisposed() ) {
            if ( !display.readAndDispatch() ) {
                display.sleep();
            }
        }

        return meta;
    }

    private void dispose() {
        WindowProperty winprop = new WindowProperty( shell );
        props.setScreen( winprop );
        shell.dispose();
    }

    /**
     * Copy information from the meta-data input to the dialog fields.
     */
    public void populateDialog() {
        wName.setText(Const.nullToEmpty( meta.getName()) );
        wFilename.setText(Const.nullToEmpty( meta.getFilename()) );
        wOutputFolder.setText(Const.nullToEmpty( meta.getOutput()) );
        wTempDir.setText(Const.nullToEmpty( meta.getTemp()) );
        wForecastSteps.setText(Const.nullToEmpty( meta.getForecastSteps()) );
        wConfigPath.setText(Const.nullToEmpty( meta.getConfigPath()) );
        wModelName.setText( Const.nullToEmpty(meta.getModelName()) );
        wToLoadFile.setText(Const.nullToEmpty( meta.getToLoadFile()) );
        wTarget.setText( Const.nullToEmpty( meta.getTarget()) );
        wRunConfiguration.select( meta.getRunConfiguration() );

        wName.selectAll();
        wName.setFocus();
    }

    private void cancel() {
        meta.setChanged( changed );
        meta = null;
        dispose();
    }

    /**
     * This method is called once the dialog is confirmed. It may only close the window if the
     * job entry has a non-empty name.
     */
    private void ok() {
        if (Utils.isEmpty(wName.getText()) ) {
            MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
            mb.setText( BaseMessages.getString( PKG, "System.StepJobEntryNameMissing.Title" ) );
            mb.setMessage( BaseMessages.getString( PKG, "System.JobEntryNameMissing.Msg" ) );
            mb.open();
            return;
        }

        meta.setName( wName.getText() );
        meta.setFilename( wFilename.getText() );
        meta.setOutput( wOutputFolder.getText() );
        meta.setTemp( wTempDir.getText() );
        meta.setForecastSteps( wForecastSteps.getText() );
        meta.setConfigPath( wConfigPath.getText() );
        meta.setModelName( wModelName.getText() );
        meta.setToLoadFile( wToLoadFile.getText() );
        meta.setTarget( wTarget.getText() );
        meta.setRunConfiguration( wRunConfiguration.getSelectionIndex() );

        dispose();
    }
}
