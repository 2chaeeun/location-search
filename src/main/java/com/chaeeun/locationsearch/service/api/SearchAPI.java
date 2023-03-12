package com.chaeeun.locationsearch.service.api;

import com.chaeeun.locationsearch.domain.Place;
import reactor.core.publisher.Mono;
import java.util.List;

public interface SearchAPI {
  Mono<List<Place>> search(String keyword);
}
