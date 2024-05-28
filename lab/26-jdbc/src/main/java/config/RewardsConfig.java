package config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.jdbc.core.JdbcTemplate;
import rewards.RewardNetwork;
import rewards.internal.RewardNetworkImpl;
import rewards.internal.account.AccountRepository;
import rewards.internal.account.JdbcAccountRepository;
import rewards.internal.restaurant.JdbcRestaurantRepository;
import rewards.internal.restaurant.RestaurantRepository;
import rewards.internal.reward.JdbcRewardRepository;
import rewards.internal.reward.RewardRepository;

@Configuration
public class RewardsConfig {

    private final JdbcTemplate jdbcTemplate;

	public RewardsConfig(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Bean
	public RewardNetwork rewardNetwork(){
		return new RewardNetworkImpl(
			accountRepository(), 
			restaurantRepository(), 
			rewardRepository());
	}
	
	@Bean
	public AccountRepository accountRepository(){
        return new JdbcAccountRepository(jdbcTemplate);
	}
	
	@Bean
	public RestaurantRepository restaurantRepository(){
        return new JdbcRestaurantRepository(jdbcTemplate);
	}
	
	@Bean
	public RewardRepository rewardRepository(){
        return new JdbcRewardRepository(jdbcTemplate);
	}
	
}
