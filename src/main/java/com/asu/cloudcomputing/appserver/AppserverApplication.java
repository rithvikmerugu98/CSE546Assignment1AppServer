package com.asu.cloudcomputing.appserver;

import com.asu.cloudcomputing.classifier.ClassificationHandler;
import com.asu.cloudcomputing.models.ImageDetail;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;

@SpringBootApplication
@RestController
@EnableScheduling
public class AppserverApplication {

	private static ClassificationHandler handler;

	public static void main(String[] args) {
		handler = new ClassificationHandler();
		SpringApplication.run(AppserverApplication.class, args);
	}

	@GetMapping("/classifyImage")
	@Scheduled(fixedRate = 15000)
	public String classifyImage() {
		System.out.println("Invoking the classify image api at " + LocalTime.now());
		List<ImageDetail> imageDetails = handler.saveImagesFromQueue();
		try {
			imageDetails = handler.classifyImages(imageDetails);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Writing the response to response SQS Queue
		handler.writeToResponseQueue(imageDetails);
		// Deleting the entries in request SQS Queue
		handler.deleteFromRequestQueue(imageDetails);
		// Save the images in the bucket
		handler.saveToS3(imageDetails);
		// Delete locally saved files
		handler.deleteFilesLocally(imageDetails);
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		try {
			return ow.writeValueAsString(imageDetails);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	@GetMapping("/healthCheck")
	public int healthCheck() {
		return 200;
	}

	@GetMapping("/terminateInstance")
	public void terminate() {
		handler.terminateInstance();
	}

}
