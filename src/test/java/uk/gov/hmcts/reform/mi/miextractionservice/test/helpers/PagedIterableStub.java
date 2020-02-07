package uk.gov.hmcts.reform.mi.miextractionservice.test.helpers;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.mockito.Mockito.mock;

@SuppressWarnings("unchecked")
public class PagedIterableStub<T> extends PagedIterable<T> {

    private final List<T> target;

    public PagedIterableStub(T... target) {
        super(mock(PagedFlux.class));
        this.target = Arrays.asList(target);
    }

    @Override
    public Iterator<T> iterator() {
        return target.iterator();
    }
}
