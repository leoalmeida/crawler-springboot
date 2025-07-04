package space.lasf.crawler_app.service;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import space.lasf.crawler_app.dto.CrawlDto;
import space.lasf.crawler_app.entity.Crawler;
import space.lasf.crawler_app.mapper.CrawlerMapper;
import space.lasf.crawler_app.repository.CrawlerRepository;

/**
 * Implementação do serviço para gerenciamento de clientes.
 */
@Service
public class RequestServiceImpl implements RequestService {
 
    @Autowired
    private CrawlerRepository crawlRepository;
    
    private static final int CODELENGTH = 8;
    
    private static final String SALT = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    
    @Override
    @Transactional
    public Optional<Crawler> createRequest(String keyword) {
        if (null==keyword || keyword.isEmpty() || keyword.trim().length() < 4 || keyword.trim().length() > 32){
            throw new IllegalArgumentException("The keyword should have between 4 and 32 characteres");
        }        
        String randomCode = generateRandomCode();
        System.out.println("Generated Random Code: " + randomCode);

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
    public Crawler endRequest(Crawler request){
        if (null==request){
            throw new IllegalArgumentException("The request should be active");
        }
        request.endProcess();
        System.out.println("Ending: " + request.getSearchKey());

        return crawlRepository.save(request);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
    public Optional<CrawlDto> findRequestByKey(String key) {
        return crawlRepository.findRequestByKeyAndStatus(key)
                        .map(CrawlerMapper::toCrawlDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrawlDto> findAllRequests() {
        return crawlRepository.findAll().stream()
                .map(CrawlerMapper::toCrawlDto)
                .toList();
    }

    public static String generateRandomCode() {
        // Define the characters to be used in the random code
        StringBuilder codeBuilder = new StringBuilder();
        try {
            SecureRandom random = SecureRandom.getInstanceStrong();

            for (int i = 0; i < CODELENGTH; i++) {
                // Get a random index within the range of available characters
                int randomIndex = random.nextInt(SALT.length());
                // Append the character at the random index to the code
                codeBuilder.append(SALT.charAt(randomIndex));
            }
        } catch (NoSuchAlgorithmException e) {
            Random random = new Random();
            for (int i = 0; i < CODELENGTH; i++) {
                // Get a random index within the range of available characters
                int randomIndex = random.nextInt(SALT.length());
                // Append the character at the random index to the code
                codeBuilder.append(SALT.charAt(randomIndex));
            }
        }
        return codeBuilder.toString();
    }
}