package com.asu.cloudcomputing.appserver;

import com.asu.cloudcomputing.classifier.ClassificationHandler;
import com.asu.cloudcomputing.models.ImageDetail;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@SpringBootApplication
@RestController
public class AppserverApplication {

	private static ClassificationHandler handler;

	public static void main(String[] args) {
		handler = new ClassificationHandler();
		SpringApplication.run(AppserverApplication.class, args);
	}

	@GetMapping("/classifyImage")
	public String classifyImage() {
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

	@GetMapping("/terminateInstance")
	public void terminate() {
		handler.terminateInstance();
	}

}
