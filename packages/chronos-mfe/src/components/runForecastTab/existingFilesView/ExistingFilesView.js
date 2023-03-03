import classNames from 'classnames';
import React, { useState, useEffect } from 'react';
import { useFormContext } from 'react-hook-form';
import styles from './existing-files-view.scss';
// Container components
import SelectBox from 'dna-container/SelectBox';
import { regionalDateAndTimeConversionSolution } from '../../../utilities/utils';
import { chronosApi } from '../../../apis/chronos.api';


const ExistingFilesView = ({projectId, setShowExistingFiles, setInputFile, setIsExistingInputFile}) => {
  const {register} = useFormContext();
  const [savedFiles, setSavedFiles] = useState([]);
  const [selectedInputFile, setSelectedInputFile] = useState();
  const [error] = useState(false);

  useEffect(() => {
    SelectBox.defaultSetup();
    //eslint-disable-next-line
  }, []);

  useEffect(() => {
    chronosApi.getAllInputFiles(projectId).then((res) => {
      if(res.data !== '') {
        setSavedFiles(res.data.files);
      }
      // setSavedFiles(savedInputs);
      SelectBox.defaultSetup();
    }).catch(error => {
      Notification.show(
        error?.response?.data?.response?.errors?.[0]?.message || error?.response?.data?.response?.warnings?.[0]?.message || 'Error while fetching input files',
        'alert',
      );
    });
    //eslint-disable-next-line
  }, [projectId]);

  const selectedSavedFile = (e) => {
    const selectedOne = savedFiles.filter(item => item.path === e.target.value);
    const selectedInput = {...selectedOne[0]};
    setSelectedInputFile(selectedInput);
  }
  
  return (
    <div className={styles.existingFilesContainer}>
      <div className={styles.flexLayout}>
        {
          savedFiles.length !== 0 ? 
          <div className={classNames(`input-field-group include-error ${error ? 'error' : ''}`)}>
            <label id="savedInputPathLabel" htmlFor="existingFilenField" className="input-label">
              Input File <sup>*</sup>
            </label>
            <div className="custom-select" 
              // onBlur={() => trigger('savedInputPath')}
              >
              <select
                id="savedInputPath"
                required={true}
                onChange={(e) => {selectedSavedFile(e)}}
              >
                <option id="savedInputPathOption" value={0}>
                  Choose
                </option>
                {savedFiles.map((file) => (
                  <option id={file.name} key={file.id} value={file.path}>
                    {file.name}
                  </option>
                ))}
              </select>
            </div>
            <span className={classNames('error-message')}>{error && '*Missing entry'}</span>
          </div> : null
        }
      </div>
      {savedFiles.length === 0 && <span>No saved input files</span>}
      {
        selectedInputFile?.path !== undefined &&
          <>
            <p>{selectedInputFile?.name}</p>
            <div className={styles.flexLayout}>
              <div className={styles.fullWidth}>
                <div className={styles.uploadInfo}>
                  <span>Uploaded On</span>
                  <span>{regionalDateAndTimeConversionSolution(selectedInputFile?.createdOn)}</span>
                </div>
                <div className={styles.uploadInfo}>
                  <span>Uploaded By</span>
                  <span>{selectedInputFile?.createdBy}</span>
                </div>
              </div>
            </div>
          </>
      }
      { savedFiles.length !== 0 && 
        <>
          <hr />
          <div className={styles.btnContinue}>
            <button
              className="btn btn-primary"
              type="submit"
              disabled={savedFiles.length === 0 ? true : false}
              onClick={() => {
                setShowExistingFiles(false);
                setInputFile([selectedInputFile]);
                register('savedInputPath', { value: selectedInputFile.path });
                setIsExistingInputFile(true);
              }}
            >
              Continue with file
            </button>
          </div>
        </>
      }
    </div>
  );
}

export default ExistingFilesView;