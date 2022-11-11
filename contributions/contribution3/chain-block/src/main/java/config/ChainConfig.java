package config;

import enums.ContractType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author jie.zhong
 * @version 1.0
 * @date 2022/9/8 15:45
 */
@Data
@ConfigurationProperties(prefix = "block.chain")
@Component
public class ChainConfig {


    private String configPath;


    private String abiPath;


    private Map<ContractType, Contract> contracts;


    @Data
    public static class Contract {

        private String contractName;

        private Integer groupId;

        private String contractAddress;
    }


}
