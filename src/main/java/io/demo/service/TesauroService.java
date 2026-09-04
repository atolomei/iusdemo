package io.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class TesauroService extends BaseService {

	public TesauroService(Settings settings) {
		super(settings);

	}

	public List<String> classify(String text) {
		return List.of("t1", "t2", "t3");
	}

}
