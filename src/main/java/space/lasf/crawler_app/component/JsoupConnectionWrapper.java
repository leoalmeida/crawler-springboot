package space.lasf.crawler_app.component;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * A wrapper around Jsoup's static connect method to make it testable.
 */
@Component
public class JsoupConnectionWrapper {

    public Document connect(String url) throws IOException {
        return Jsoup.connect(url).get();
    }
}