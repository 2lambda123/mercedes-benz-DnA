import React from 'react';
import styles from './visual-container.scss';
import html2canvas from 'html2canvas';
import { jsPDF } from 'jspdf';
import ContextMenu from '../contextMenu/ContextMenu';
import Spinner from '../spinner/Spinner';
import Plot from 'react-plotly.js';

const VisualContainer = ({title, forecastRun, printRef, loading, forecastData, addTraces, layout}) => {
  const exportToPdf = async () => {
    const element = printRef.current;
    const canvas = await html2canvas(element);
    const data = canvas.toDataURL('image/png');

    const pdf = new jsPDF('l');
    const imgProperties = pdf.getImageProperties(data);
    const pdfWidth = pdf.internal.pageSize.getWidth();
    const pdfHeight =
      (imgProperties.height * pdfWidth) / imgProperties.width;

    pdf.addImage(data, 'PNG', 0, 0, pdfWidth, pdfHeight);
    pdf.save(forecastRun?.runName + '.pdf');
  }
  const exportToPng = async () => {
    const element = printRef.current;
    const canvas = await html2canvas(element);

    const data = canvas.toDataURL('image/png');
    const link = document.createElement('a');

    if (typeof link.download === 'string') {
      link.href = data;
      link.download = forecastRun?.runName + '.png';

      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } else {
      window.open(data);
    }
  }
  const contextMenuItems = [
    {
      title: 'Export to PDF',
      onClickFn: exportToPdf
    },
    {
      title: 'Export to PNG',
      onClickFn: exportToPng
    }
  ];
  return (
    <div className={styles.content}>
      <div className={styles.header}>
        <h3>{title}</h3>
        <div className={styles.actionMenu}>
          <ContextMenu id={'visual'} items={contextMenuItems} />
        </div>
      </div>
      <div className={styles.firstPanel} ref={printRef}>
        { loading && <Spinner /> }
        { !loading && forecastData.length === 0 && <p>No visualization for the given data.</p> }
        { !loading && forecastData.length > 0 &&
            <>
              <p className={styles.chartLabel}>Forecast</p>
              <div className={styles.chartContainer}>
                <Plot
                  data={addTraces(forecastData)}
                  layout={layout}
                  useResizeHandler
                  config={{ displaylogo: false }}
                  style={{width: '100%', height: '450px'}}
                />
              </div>
            </>
        }
      </div>
    </div>
  );
}

export default VisualContainer;