import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './styles.scss';
import MDEditor from '@uiw/react-md-editor';

import { useDispatch, useSelector } from 'react-redux';

import { Link, withRouter, useParams } from 'react-router-dom';

import { dataProductApi } from '../../../apis/dataproducts.api';
import { hostServer } from '../../../server/api';

import DataTranferCardLayout from '../../dataTransfer/Layout/CardView/DataTransferCardItem';

import { setSelectedData, setDivisionList } from '../redux/dataProductSlice';

import { regionalDateFormat } from '../../../Utility/utils';
import mockData from '../data.json';

import InfoModal from 'dna-container/InfoModal';
import Modal from 'dna-container/Modal';
import ConfirmModal from 'dna-container/ConfirmModal';

import ProgressIndicator from '../../../common/modules/uilab/js/src/progress-indicator';
import Tabs from '../../../common/modules/uilab/js/src/tabs';

import lockIcon from '../../../assets/lockIcon.png';
import selfserviceImg from '../../../assets/selfservice.png';

import ConsumerForm from '../../dataTransfer/ConsumerForm';
import { deserializeFormData, serializeDivisionSubDivision } from '../../../Utility/formData';

const Summary = ({ history, user }) => {
  const { id: dataProductId } = useParams();
  const { selectedData: data, data: dataList, divisionList } = useSelector((state) => state.dataProduct);

  const dispatch = useDispatch();

  const [showDeleteModal, setShowDeleteModal] = useState(false);

  const [currentTab, setCurrentTab] = useState('provider');

  const [showInfoModal, setShowInfoModal] = useState(false);
  const [step, setStep] = useState(0);

  const [showRequestAccessModal, setShowRequestAccessModal] = useState(false);
  const [accessRequested, setAccessRequest] = useState(false);

  const isCreator = data.providerInformation?.createdBy?.id === user?.id || true;

  const showContactInformation = data?.openSegments?.includes('ContactInformation');
  const showConfidentiality = data?.openSegments?.includes('ClassificationAndConfidentiality');
  const showPersonalData = data?.openSegments?.includes('IdentifyingPersonalRelatedData');
  const showTransNationalData = data?.openSegments?.includes('IdentifiyingTransnationalDataTransfer');
  const showDeletionRequirements = data?.openSegments?.includes('SpecifyDeletionRequirements');

  const division = serializeDivisionSubDivision(divisionList, {
    division: data.division,
    subDivision: data.subDivision,
  });

  useEffect(() => {
    ProgressIndicator.show();
    hostServer.get('/divisions').then((res) => {
      dispatch(setDivisionList(res.data));
      ProgressIndicator.hide();
    });
  }, [dispatch]);

  useEffect(() => {
    getDataProductById();

    return () => {
      dispatch(setSelectedData({}));
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dataList]);

  const getDataProductById = () => {
    dataProductApi.getDataProductById(dataProductId).then((res) => {
      if (res.status === 204) {
        return history.push('/NotFound');
      } else {
        const data = deserializeFormData({ item: res.data, isDataProduct: true });
        dispatch(setSelectedData(data));
        Tabs.defaultSetup();
      }
    });
  };

  useEffect(() => {
    const mainPanel = document.getElementById('mainPanel');
    const accessBtnDiv = document.querySelector('.accessBtn');
    const handleScroll = () => {
      if (window.scrollY + window.innerHeight >= mainPanel.scrollHeight) {
        accessBtnDiv.classList.remove('accessBtn');
        accessBtnDiv.classList.add('accessBtnFixed');
      } else {
        accessBtnDiv.classList.add('accessBtn');
        accessBtnDiv.classList.remove('accessBtnFixed');
      }
    };

    window.addEventListener('scroll', handleScroll);

    return () => window.removeEventListener('scroll', handleScroll);

    //eslint-disable-next-line
  }, []);

  useEffect(() => {
    // update colors for the markdown editor
    const mdEditor = document.querySelector('[data-color-mode="dark"]>div.wmde-markdown');

    mdEditor.style.setProperty('--color-canvas-default', 'transparent');
    mdEditor.style.setProperty('font-size', 'inherit');
  }, []);

  const setTab = (e) => {
    // e.preventDefault();
    setCurrentTab(e.target.id);
  };

  const deleteDataProductAccept = () => {
    ProgressIndicator.show();
    dataProductApi.deleteDataProduct(data?.id).then(() => {
      history.push('/dataproducts');
      setShowDeleteModal(false);
    });
  };
  const deleteDataProductClose = () => {
    setShowDeleteModal(false);
  };

  const deleteDataProductContent = (
    <div>
      <h3>Are you sure you want to delete {data?.productName} ? </h3>
    </div>
  );

  const infoModalHeaderContent = (
    <div className={Styles.titleContainer}>
      <div>
        <img src={lockIcon} style={{ width: '50px', marginRight: 20 }} />
      </div>
      <div>
        <h2>How to access the data</h2>
        <span>Follow the shown steps to gain access and use this product.</span>
      </div>
    </div>
  );

  const infoModalContent = (
    <>
      <hr className={Styles.line} />
      <div className={Styles.modalContent}>
        <div>
          {' '}
          <h5>Step 1 - Role Request</h5>
          <div>
            First visit the Role Request Self Service and request the &ldquo;Exploration Self Service ACDOCA Full Scope
            (OneERP)&rdquo; - role in SBISS/CarLA/Core as shown on the right.{' '}
          </div>
        </div>
        <div>
          <img src={selfserviceImg} className={Styles.imgGuide} />
          <div className={Styles.bullets}>
            {[...Array(4)].map((i, ind) => (
              <span onClick={() => setStep(ind)} className={ind === step ? Styles.activeBullet : ''} key={ind}></span>
            ))}
          </div>
        </div>
      </div>
      <hr className={Styles.line} />
      <div className={Styles.modalContent}>
        <div>
          {' '}
          <h5>Step 2 - Get started</h5>
          <div>We will notify you via E-Mail as soon as you can access and use the data product.</div>
        </div>
      </div>
      <hr className={Styles.line} />
    </>
  );

  const requestAccessModalContent = (
    <>
      <ConsumerForm isDataProduct={true} />
    </>
  );

  return (
    <div className="dataproductSummary">
      <div id="mainPanel" className={Styles.mainPanel}>
        <div>
          <button className="btn btn-text back arrow" type="submit" onClick={() => history.goBack()}>
            Back
          </button>
          {isCreator ? (
            <div className={Styles.actionBtns}>
              <button
                className="btn btn-primary"
                onClick={() =>
                  history.push({ pathname: '/dataproduct/create', state: { copyId: data?.dataProductId } })
                }
              >
                <i className="icon mbc-icon copy" tooltip-data="Create Copy"></i>Copy & Create New
              </button>
              <button className="btn btn-primary" onClick={() => setShowDeleteModal(true)}>
                <i className="icon mbc-icon delete-new" tooltip-data="Delete"></i>Delete
              </button>
              <button
                className="btn btn-primary"
                onClick={() => history.push(`/dataproduct/edit/${data?.dataProductId}`)}
              >
                <i className="icon mbc-icon edit fill" tooltip-data="Edit"></i>Edit
              </button>
            </div>
          ) : null}
          <div className={Styles.summaryBannerTitle}>
            <h2>{data?.productName}</h2>
          </div>
          <div id="data-product-summary-tabs" className="tabs-panel">
            <div className="tabs-wrapper">
              <nav>
                <ul className="tabs">
                  <li className={currentTab === 'provider' ? 'tab active' : 'tab'}>
                    <a href="#tab-content-1" id="provider" onClick={setTab}>
                      Data Product Summary
                    </a>
                  </li>
                  <li className={'tab disabled'}>
                    <a id="summary1" className={'hidden'}>
                      `
                    </a>
                  </li>
                  <li className={'tab disabled'}>
                    <a id="summary2" className={'hidden'}>
                      `
                    </a>
                  </li>
                  <li className={'tab disabled'}>
                    <a id="summary3" className={'hidden'}>
                      `
                    </a>
                  </li>
                  <li className={'tab disabled'}>
                    <a id="summary4" className={'hidden'}>
                      `
                    </a>
                  </li>
                  <li className={'tab disabled'}>
                    <a id="summary5" className={'hidden'}>
                      `
                    </a>
                  </li>
                </ul>
              </nav>
            </div>
            <div className="tabs-content-wrapper">
              <div id="tab-content-1" className="tab-content">
                <div className={Styles.sectionWrapper}>
                  <div className={Styles.firstPanel}>
                    <div className={Styles.flexLayout}>
                      <div>
                        <h5>Description</h5>
                      </div>
                    </div>
                    <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
                      <div>
                        <label className="input-label summary">Data Product Name</label>
                        <br />
                        {data.productName}
                      </div>
                      <div>
                        <label className="input-label summary">Agile Release Train</label>
                        <br />
                        {data.ART?.name || '-'}
                      </div>
                      <div>
                        <label className="input-label summary">CarLA Function</label>
                        <br />
                        {data.carLAFunction?.name || '-'}
                      </div>
                      <div>
                        <label className="input-label summary">Corporate Data Catalog</label>
                        <br />
                        {data.corporateDataCatalog?.name || '-'}
                      </div>
                    </div>
                    <div className={Styles.flexLayout}>
                      <div>
                        <label className="input-label summary">Data Product Description</label>
                        <br />
                        {data.description}
                      </div>
                    </div>
                    <div className={Styles.flexLayout}>
                      <div data-color-mode="dark">
                        <label className="input-label summary">How to access data catalog</label>
                        <br />
                        <MDEditor.Markdown source={data.howToAccessText} />
                      </div>
                    </div>
                  </div>
                </div>
                {showContactInformation ? (
                  <div className={Styles.sectionWrapper}>
                    <div className={Styles.firstPanel}>
                      <div className={Styles.flexLayout}>
                        <div>
                          <h5>Contact Information</h5>
                        </div>
                      </div>
                      <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
                        <div>
                          <label className="input-label summary">Information Owner</label>
                          <br />
                          {data.informationOwner?.firstName} {data.informationOwner?.lastName}
                        </div>
                        <div>
                          <label className="input-label summary">Publish Date of Data Product</label>
                          <br />
                          {regionalDateFormat(data.dateOfDataProduct)}
                        </div>
                        <div>
                          <label className="input-label summary">Name</label>
                          <br />
                          {data?.name?.firstName} {data?.name?.lastName}
                        </div>
                        <div>
                          <label className="input-label summary">Division</label>
                          <br />
                          {division?.name}
                        </div>
                      </div>
                      <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
                        <div>
                          <label className="input-label summary">Sub Division</label>
                          <br />
                          {division?.subdivision?.name || '-'}
                        </div>
                        <div>
                          <label className="input-label summary">Department</label>
                          <br />
                          {data.department}
                        </div>
                        <div>
                          <label className="input-label summary">PlanningIT App-ID</label>
                          <br />
                          {data.planningIT || '-'}
                        </div>
                        <div>
                          <label className="input-label summary">Compliance Officer / Responsible (LCO/LCR) </label>
                          <br />
                          {data.complianceOfficer}
                        </div>
                      </div>
                    </div>
                  </div>
                ) : null}
                {showConfidentiality ? (
                  <div className={Styles.sectionWrapper}>
                    <div className={Styles.firstPanel}>
                      <div className={Styles.flexLayout}>
                        <div>
                          <h5>Data Description & Classification</h5>
                        </div>
                      </div>
                      <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                        <div>
                          <label className="input-label summary">Description & Classification of transfered data</label>
                          <br />
                          {data.classificationOfTransferedData}
                        </div>
                        <div>
                          <label className="input-label summary">Confidentiality</label>
                          <br />
                          {data?.confidentiality}
                        </div>
                      </div>
                    </div>
                  </div>
                ) : null}
                {showPersonalData ? (
                  <div className={Styles.sectionWrapper}>
                    <div className={Styles.firstPanel}>
                      <div className={Styles.flexLayout}>
                        <div>
                          <h5>Personal Related Data</h5>
                        </div>
                      </div>
                      <div className={Styles.flexLayout}>
                        <div>
                          <label className="input-label summary">Is data personal related</label>
                          <br />
                          {data.personalRelatedData}
                        </div>
                      </div>
                      {data.personalRelatedData === 'Yes' ? (
                        <div className={classNames(Styles.flexLayout, Styles.fourColumn)}>
                          <div>
                            <label className="input-label summary">Description</label>
                            <br />
                            {data.personalRelatedDataDescription}
                          </div>
                          <div>
                            <label className="input-label summary">
                              Original (business) purpose of processing this personal related data
                            </label>
                            <br />
                            {data.personalRelatedDataPurpose}
                          </div>
                          <div>
                            <label className="input-label summary">
                              Original legal basis for processing this personal related data
                            </label>
                            <br />
                            {data.personalRelatedDataLegalBasis}
                          </div>
                          <div></div>
                        </div>
                      ) : null}
                    </div>
                  </div>
                ) : null}
                {showTransNationalData ? (
                  <div className={Styles.sectionWrapper}>
                    <div className={Styles.firstPanel}>
                      <div className={Styles.flexLayout}>
                        <div>
                          <h5>Trans-national Data</h5>
                        </div>
                      </div>
                      <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                        <div>
                          <label className="input-label summary">
                            Is data being transferred from one country to another?
                          </label>
                          <br />
                          {data.transnationalDataTransfer}
                        </div>
                        {data.transnationalDataTransfer == 'Yes' ? (
                          <div>
                            <label className="input-label summary">Is one of these countries not within the EU?</label>
                            <br />
                            {data.transnationalDataTransferNotWithinEU || 'No'}
                          </div>
                        ) : null}
                        {data.transnationalDataTransfer == 'Yes' &&
                        data.transnationalDataTransferNotWithinEU == 'Yes' ? (
                          <div>
                            <label className="input-label summary">Has LCO/LCR approved this data transfer?</label>
                            <br />
                            {data.LCOApprovedDataTransfer}
                          </div>
                        ) : null}
                        <div>
                          <label className="input-label summary">Does product contain insider information?</label>
                          <br />
                          {data.insiderInformation}
                        </div>
                      </div>
                      <div className={Styles.flexLayout}>
                        <div>
                          <label className="input-label summary">Is data from China included?</label>
                          <br />
                          {data.dataOriginatedFromChina}
                        </div>
                      </div>
                    </div>
                  </div>
                ) : null}
                {showDeletionRequirements ? (
                  <div className={Styles.sectionWrapper}>
                    <div className={Styles.firstPanel}>
                      <div className={Styles.flexLayout}>
                        <div>
                          <h5>Deletion Requirements & Other</h5>
                        </div>
                      </div>
                      <div className={classNames(Styles.flexLayout, Styles.threeColumn)}>
                        <div>
                          <label className="input-label summary">
                            Are there specific deletion requirements for this data?
                          </label>
                          <br />
                          {data.deletionRequirement}
                        </div>
                        {data.deletionRequirement === 'Yes' ? (
                          <div>
                            <label className="input-label summary">Describe deletion requirements</label>
                            <br />
                            {data.deletionRequirementDescription}
                          </div>
                        ) : null}
                        <div></div>
                      </div>
                      <div className={Styles.flexLayout}>
                        <div>
                          <label className="input-label summary">Other relevant information </label>
                          <br />
                          {data.otherRelevantInfo || '-'}
                        </div>
                      </div>
                    </div>
                  </div>
                ) : null}
              </div>
            </div>
          </div>
          <hr className={Styles.line} />
          <div className={Styles.dataTransferSection}>
            <h3>{`Data Transfer ${mockData?.result?.length ? `(${mockData?.result?.length})` : null}`}</h3>
            <div>
              <Link to={'/datasharing'} target="_blank" rel="noreferrer noopener">
                Show in Data Sharing
                <i tooltip-data="Open in New Tab" className={'icon mbc-icon new-tab'} />
              </Link>
            </div>
          </div>
          <div className={classNames(Styles.allDataproductCardviewContent)}>
            {mockData.result?.map((product, index) => {
              return <DataTranferCardLayout key={index} product={product} user={user} isDataProduct={true} />;
            })}
          </div>
        </div>
      </div>
      <div className={'accessBtn'}>
        {accessRequested ? (
          <button className="btn btn-tertiary" type="button" onClick={() => setShowInfoModal(true)}>
            How to access
          </button>
        ) : (
          <button className="btn btn-tertiary" type="button" onClick={() => setShowRequestAccessModal(true)}>
            Request access
          </button>
        )}
      </div>
      {showInfoModal && (
        <InfoModal
          title="How to access data"
          show={showInfoModal}
          customHeader={infoModalHeaderContent}
          content={infoModalContent}
          onCancel={() => {
            setShowInfoModal(false);
            setAccessRequest(true);
          }}
        />
      )}
      {showRequestAccessModal && (
        <Modal
          title="Request access"
          show={showRequestAccessModal}
          showAcceptButton={false}
          showCancelButton={false}
          scrollableContent={true}
          modalWidth={'90%'}
          content={requestAccessModalContent}
          onCancel={() => {
            setAccessRequest(true);
            setShowRequestAccessModal(false);
          }}
        />
      )}
      <ConfirmModal
        title={''}
        acceptButtonTitle="Yes"
        cancelButtonTitle="No"
        showAcceptButton={true}
        showCancelButton={true}
        show={showDeleteModal}
        content={deleteDataProductContent}
        onCancel={deleteDataProductClose}
        onAccept={deleteDataProductAccept}
      />
    </div>
  );
};

export default withRouter(Summary);