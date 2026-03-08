package space.lasf.crawler_app.component;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

/**
 * A wrapper around Jsoup's static connect method to make it testable.
 */
@Component
public class JsoupConnectionWrapper {

    public Document connect(final String url) throws IOException {
        return Jsoup.connect(url).get();
    }
}
