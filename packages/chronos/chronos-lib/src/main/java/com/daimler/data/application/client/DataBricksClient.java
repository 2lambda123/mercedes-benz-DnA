package com.daimler.data.application.client;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.dto.databricks.DatabricksJobRunNowRequestDto;
import com.daimler.data.dto.databricks.DatabricksRunGenericRequestDto;
import com.daimler.data.dto.databricks.RunNowNotebookParamsDto;
import com.daimler.data.dto.forecast.DataBricksErrorResponseVO;
import com.daimler.data.dto.forecast.JobRunsListVO;
import com.daimler.data.dto.forecast.RunDetailsVO;
import com.daimler.data.dto.forecast.RunNowResponseVO;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DataBricksClient {

	@Value("${databricks.uri.base}")
	private String dataBricksBaseUri;
	
	@Value("${databricks.uri.runnow}")
	private String dataBricksJobTriggerRunNowPath;
	
	@Value("${databricks.uri.deleterun}")
	private String dataBricksJobDeleteRunPath;
	
	@Value("${databricks.uri.getrun}")
	private String dataBricksJobGetRunPath;
	
	@Value("${databricks.uri.jobrunlist}")
	private String dataBricksJobRunList;
	
	@Value("${databricks.jobId}")
	private String dataBricksJobId;
	
	@Value("${databricks.defaultConfigYml}")
	private String dataBricksJobDefaultConfigYml;
	
	@Value("${databricks.pat}")
	private String dataBricksPAT;
	
	@Autowired
	HttpServletRequest httpRequest;
	
	@Autowired
	private RestTemplate restClient;
	
	public RunNowResponseVO runNow(String runCorrelationUUID, RunNowNotebookParamsDto notebookParams) {
		RunNowResponseVO runNowResponse = null;
		try {
				HttpHeaders headers = new HttpHeaders();
				headers.set("Accept", "application/json");
				headers.set("Authorization", "Bearer "+dataBricksPAT);
				headers.setContentType(MediaType.APPLICATION_JSON);
				
				String runNowUrl = dataBricksBaseUri + dataBricksJobTriggerRunNowPath;
				DatabricksJobRunNowRequestDto requestWrapper = new DatabricksJobRunNowRequestDto();
				if(notebookParams.getConfig()==null || "".equalsIgnoreCase(notebookParams.getConfig()))
					notebookParams.setConfig(dataBricksJobDefaultConfigYml);
				requestWrapper.setJob_id(dataBricksJobId);
				requestWrapper.setNotebook_params(notebookParams);
				
				HttpEntity<DatabricksJobRunNowRequestDto> requestEntity = new HttpEntity<>(requestWrapper,headers);
				ResponseEntity<RunNowResponseVO> response = restClient.exchange(runNowUrl, HttpMethod.POST,
						requestEntity, RunNowResponseVO.class);
				if (response.hasBody()) {
					runNowResponse = response.getBody();
				}
				}catch(Exception e) {
					e.printStackTrace();
				}
			return runNowResponse;
	}
	
	
	public DataBricksErrorResponseVO deleteRun(String runId) {
		DataBricksErrorResponseVO deleteRunResponse = null;
		try {
				HttpHeaders headers = new HttpHeaders();
				headers.set("Accept", "application/json");
				headers.set("Authorization", "Bearer "+dataBricksPAT);
				headers.setContentType(MediaType.APPLICATION_JSON);
				
				String deleteRunUrl = dataBricksBaseUri + dataBricksJobDeleteRunPath;
				DatabricksRunGenericRequestDto requestWrapper = new DatabricksRunGenericRequestDto();
				requestWrapper.setRun_id(runId);
				HttpEntity<DatabricksRunGenericRequestDto> requestEntity = new HttpEntity<>(requestWrapper,headers);
				ResponseEntity<DataBricksErrorResponseVO> response = restClient.exchange(deleteRunUrl, HttpMethod.POST,
						requestEntity, DataBricksErrorResponseVO.class);
				if (response.hasBody()) {
					deleteRunResponse = response.getBody();
				}
				}catch(Exception e) {
					e.printStackTrace();
				}
			return deleteRunResponse;
	}
	
	
	public RunDetailsVO getSingleRun(String runId) {
		RunDetailsVO getSingleRunResponse = null;
		try {
				HttpHeaders headers = new HttpHeaders();
				headers.set("Accept", "application/json");
				headers.set("Authorization", "Bearer "+dataBricksPAT);
				headers.setContentType(MediaType.APPLICATION_JSON);
				
				String getSingleRunUrl = dataBricksBaseUri + dataBricksJobGetRunPath + "?run_id=" + runId;
				HttpEntity requestEntity = new HttpEntity<>(headers);
				ResponseEntity<RunDetailsVO> response = restClient.exchange(getSingleRunUrl, HttpMethod.POST,
						requestEntity, RunDetailsVO.class);
				if (response.hasBody()) {
					getSingleRunResponse = response.getBody();
				}
				}catch(Exception e) {
					e.printStackTrace();
				}
			return getSingleRunResponse;
	}
	
	
	public JobRunsListVO getJobRuns() {
		JobRunsListVO getJobRunsResponse = null;
		try {
				HttpHeaders headers = new HttpHeaders();
				headers.set("Accept", "application/json");
				headers.set("Authorization", "Bearer "+dataBricksPAT);
				headers.setContentType(MediaType.APPLICATION_JSON);
				
				String getJobRunsUrl = dataBricksBaseUri + dataBricksJobRunList + "?active_only=true&expand_tasks=false&run_type=JOB_RUN&job_id="+dataBricksJobId;
				HttpEntity requestEntity = new HttpEntity<>(headers);
				ResponseEntity<JobRunsListVO> response = restClient.exchange(getJobRunsUrl, HttpMethod.POST,
						requestEntity, JobRunsListVO.class);
				if (response.hasBody()) {
					getJobRunsResponse = response.getBody();
				}
				}catch(Exception e) {
					e.printStackTrace();
				}
			return getJobRunsResponse;
	}
	
	
	

}