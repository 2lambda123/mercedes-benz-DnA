/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.mb.dna.kube.client.main;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1SecretList;
import io.kubernetes.client.util.Config;
import io.minio.BucketExistsArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Bucket;
import io.minio.messages.Item;

@SpringBootApplication
public class Application {

  private static String KUBEFLOW_NAMESPACE = "kubeflow";
  
  private static final Logger LOG = LoggerFactory.getLogger(Application.class);
  
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
    try {
    	ObjectMapper mapper = new ObjectMapper();
		ApiClient client = Config.defaultClient();
		Configuration.setDefaultApiClient(client);
        CoreV1Api api = new CoreV1Api();
        V1PodList items = api.listNamespacedPod(KUBEFLOW_NAMESPACE,null, null, null, null, null, null, null, null, 10, false);
        V1Pod minioPod = items.getItems().stream().filter(pod -> pod.getMetadata().getName().contains("minio")).findFirst().get();
        V1PodSpec minioPodSpec = minioPod.getSpec();
        V1Container minioContainer = minioPodSpec.getContainers().stream().filter(container -> container.getName().contains("minio")).findFirst().get();
        minioContainer.getEnv().forEach(x -> LOG.info("Environment name: " + x.getName() + " , value: " + x.getValueFrom()));
        String podIp = minioPod.getStatus().getPodIP();
        LOG.info("Pod ip is : "+ podIp);
        String minioBaseUri = "http://"+podIp;
        String minioAdminAccessKeySample = "";
    	String minioAdminSecretKeySample = "";
    	MinioClient minioClient = MinioClient.builder()
    				.endpoint(minioBaseUri+":9000")
    		        .credentials(minioAdminAccessKeySample, minioAdminSecretKeySample)
    		        .build();
    	List<Bucket> bucketList = minioClient.listBuckets();
    	Iterable<Result<Item>> results = minioClient.listObjects(
				    ListObjectsArgs.builder().bucket("models").recursive(true).build());
    	  Iterator<Result<Item>> iterator1 = results.iterator();
          while (iterator1.hasNext()) {
            Result<Item> el = iterator1.next();
            LOG.info(el.get().objectName());
          }    
          V1SecretList secretsList = api.listNamespacedSecret(KUBEFLOW_NAMESPACE, "true", null, null, null, null, null, null, null, null, false);
          List<V1Secret> secrets = secretsList.getItems();
          for(V1Secret secret : secrets) {
        	  Map<String, byte[]> secretsMap = secret.getData();
        	  for (String key: secretsMap.keySet()) {
            	  LOG.info(key + ": " + secretsMap.get(key));
              }
          }
          V1Secret result = api.readNamespacedSecret("mlpipeline-minio-artifact", KUBEFLOW_NAMESPACE, "true" );
          LOG.info("Got results successfully");
          Map<String, byte[]> secretsMap = result.getData();
          for (String key: secretsMap.keySet()) {
        	  LOG.info(key + ": " + secretsMap.get(key));
          }
	}catch(Exception e) {
		e.printStackTrace();
	}
  }


}
