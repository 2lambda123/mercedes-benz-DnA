import classNames from 'classnames';
import React, { createRef, useEffect, useRef, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import styles from './chronos-project-details.scss';
// App components
import Tabs from '../../common/modules/uilab/js/src/tabs';
import ProgressIndicator from '../../common/modules/uilab/js/src/progress-indicator';
import Notification from '../../common/modules/uilab/js/src/notification';
import RunForecastTab from '../../components/runForecastTab/RunForecastTab';
import ForecastResultsTab from '../../components/forecastResultsTab/ForecastResultsTab';
import ProjectDetailsTab from '../../components/projectDetailsTab/ProjectDetailsTab';
import Breadcrumb from '../../components/breadcrumb/Breadcrumb';
import { chronosApi } from '../../apis/chronos.api';

const tabs = {
  runForecast: {},
  forecastResults: {},
  projectDetails: {},
};

const ChronosProjectDetails = ({ user }) => {
  const { id: projectId } = useParams();

  const [currentTab, setCurrentTab] = useState('runForecast');
  const elementRef = useRef(Object.keys(tabs)?.map(() => createRef()));
  const [project, setProject] = useState();

  useEffect(() => {
    if (user?.roles?.length) {
      Tabs.defaultSetup();
    } else {
      setTimeout(() => {
        Tabs.defaultSetup();
      }, 100);
    }
  }, [user]);

  useEffect(() => {
    getProjectById();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const getProjectById = () => {
    ProgressIndicator.show();
    chronosApi.getForecastProjectById(projectId).then((res) => {
      setProject(res.data);
      ProgressIndicator.hide();
    }).catch(error => {
      Notification.show(error.message, 'alert');
      ProgressIndicator.hide();
    });
  };

  const setTab = (e) => {
    const id = e.target.id;
    if (currentTab !== id) {
      setCurrentTab(id);
    }
    e.target.id === 'runForecast' && getProjectById();
  };

  const switchTabs = (currentTab) => {
    const tabIndex = Object.keys(tabs).indexOf(currentTab) + 1;
    setCurrentTab(Object.keys(tabs)[tabIndex]);
    elementRef.current[tabIndex].click();
  };

  return (
    <>
      <div className={classNames(styles.mainPanel)}>
        <Breadcrumb>
          <li><Link to='/'>Chronos Forecasting</Link></li>
          <li>{project?.name}</li>
        </Breadcrumb>
        <h3 className={classNames(styles.title)}>{project?.name}</h3>
        <div id="data-product-tabs" className="tabs-panel">
          <div className="tabs-wrapper">
            <nav>
              <ul className="tabs">
                <li className={'tab active'}>
                  <a href="#tab-content-1" id="runForecast" ref={elementRef} onClick={setTab}>
                    Run Forecast
                  </a>
                </li>
                <li className={'tab'}>
                  <a
                    href="#tab-content-2"
                    id="forecastResults"
                    ref={(ref) => {
                      if (elementRef.current) elementRef.current[1] = ref;
                    }}
                    onClick={setTab}
                  >
                    Forecast Results
                  </a>
                </li>
                <li className={'tab'}>
                  <a
                    href="#tab-content-3"
                    id="projectDetails"
                    ref={(ref) => {
                      if (elementRef.current) elementRef.current[3] = ref;
                    }}
                    onClick={setTab}
                  >
                    Project Details
                  </a>
                </li>
              </ul>
            </nav>
          </div>
          <div className="tabs-content-wrapper">
            <div id="tab-content-1" className="tab-content">
              <RunForecastTab onRunClick={() => switchTabs(currentTab)} />
            </div>
            <div id="tab-content-2" className="tab-content">
              {currentTab === 'forecastResults' ? (
                <ForecastResultsTab />
              ) : null}
            </div>
            <div id="tab-content-3" className="tab-content">
              {currentTab === 'projectDetails' ? (
                <ProjectDetailsTab project={project} />
              ) : null}
            </div>
          </div>
        </div>
      </div>
      <div className={styles.mandatoryInfo}>* mandatory fields</div>
    </>
  );
};
export default ChronosProjectDetails;