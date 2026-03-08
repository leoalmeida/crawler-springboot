package space.lasf.crawler_app.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import space.lasf.crawler_app.component.CodeGenerator;
import space.lasf.crawler_app.dto.CrawlDto;
import space.lasf.crawler_app.entity.Crawler;
import space.lasf.crawler_app.mapper.CrawlerMapper;
import space.lasf.crawler_app.repository.CrawlerRepository;

/**
 * Implementação do serviço para gerenciamento de clientes.
 */
@Service
public class RequestServiceImpl implements IRequestService {

    private static final int KEYWORD_MIN_SIZE = 4;
    private static final int KEYWORD_MAX_SIZE = 32;
 
    @Autowired
    private CrawlerRepository crawlRepository;

    @Autowired
    private CodeGenerator codeGenerator;
    
    @Override
    @Transactional
    public Optional<Crawler> createRequest(final String keyword) {
        if (null == keyword || keyword.isEmpty()
                || keyword.trim().length() < KEYWORD_MIN_SIZE
                || keyword.trim().length() > KEYWORD_MAX_SIZE) {
            throw new IllegalArgumentException("The keyword should have between 4 and 32 characteres");
        }

        String randomCode = codeGenerator.generateRandomCode();
        
        // Valida os dados do cliente antes de salvar
        Crawler request = new Crawler();
        request.setSearchKey(randomCode);
        request.setKeyword(keyword);
        request.setUrls(new HashSet<>());
        request.startProcess();
        
        Crawler saved = crawlRepository.save(request);
        
        return Optional.of(saved);
    }

    @Override
    @Transactional
    public Crawler endRequest(final Crawler request) {
        if (null == request) {
            throw new IllegalArgumentException("The request should be active");
        }
        request.endProcess();

        return crawlRepository.save(request);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
    public Optional<CrawlDto> findRequestByKey(final String key) {
        return crawlRepository.findBySearchKey(key)
                        .map(CrawlerMapper::toCrawlDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrawlDto> findAllRequests() {
        return crawlRepository.findAll().stream()
                .map(CrawlerMapper::toCrawlDto)
                .toList();
    }
}
