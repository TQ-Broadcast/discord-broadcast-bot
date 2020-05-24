package space.delusive.discord.broadcastbot.config;

import com.google.gson.Gson;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import space.delusive.discord.broadcastbot.discord.DiscordManager;
import space.delusive.discord.broadcastbot.discord.impl.DiscordManagerImpl;
import space.delusive.discord.broadcastbot.integration.MixerIntegration;
import space.delusive.discord.broadcastbot.integration.TwitchIntegration;
import space.delusive.discord.broadcastbot.integration.YoutubeIntegration;
import space.delusive.discord.broadcastbot.integration.impl.MixerIntegrationImpl;
import space.delusive.discord.broadcastbot.integration.impl.TwitchIntegrationImpl;
import space.delusive.discord.broadcastbot.integration.impl.YoutubeIntegrationImpl;

@Configuration
@ComponentScan
@EnableScheduling
public class ApplicationConfiguration {
    @Bean
    DiscordManager getDiscordManager(
            @Value("${discord.message.pattern.youtube.video}") String youtubeVideoMessagePattern,
            @Value("${discord.message.pattern.twitch.stream}") String twitchStreamMessagePattern,
            @Value("${discord.message.pattern.mixer.stream}") String mixerStreamMessagePattern,
            @Value("${discord.channel.id}") String messageChannelId,
            @Autowired JDA jda) {
        return new DiscordManagerImpl(youtubeVideoMessagePattern, twitchStreamMessagePattern, mixerStreamMessagePattern,
                messageChannelId, jda);
    }

    @Bean
    YoutubeIntegration getYoutubeVideoRepository(
            @Value("${youtube.api.token}") String apiToken,
            @Value("${youtube.api.videos.from.playlist.url}") String videosFromPlaylistUrl,
            Gson gson) {
        return new YoutubeIntegrationImpl(videosFromPlaylistUrl, apiToken, gson);
    }

    @Bean
    TwitchIntegration getTwitchStreamRepository(
            @Value("${twitch.api.client.id}") String clientId,
            @Value("${twitch.api.client.secret}") String clientSecret,
            @Value("${twitch.api.oauth.get.token.url}") String getOAuthTokenUrl,
            @Value("${twitch.api.streams.url}") String getCurrentStreamUrl,
            @Value("${twitch.api.get.user.info.url}") String getUserInfoUrl,
            Gson gson) {
        return new TwitchIntegrationImpl(getCurrentStreamUrl, getUserInfoUrl, getOAuthTokenUrl, clientId, clientSecret,
                gson);
    }

    @Bean
    MixerIntegration getMixerStreamRepository(@Value("${mixer.api.last.stream.url}") String lastStreamUrl,
                                              @Value("${mixer.api.channel.info.by.name.url}") String channelInfoByNameUrl,
                                              Gson gson) {
        return new MixerIntegrationImpl(lastStreamUrl, channelInfoByNameUrl, gson);
    }

    @Bean
    @SneakyThrows
    JDA getJda(@Value("${discord.bot.token}") String botToken) {
        return new JDABuilder(botToken)
                .build();
    }

    @Bean
    Gson gson() {
        return new Gson();
    }

    @Bean
    EventWaiter eventWaiter() {
        return new EventWaiter();
    }

    @Bean
    PropertiesFactoryBean messages(@Value("${localization.file.name}") String fileName) {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource(fileName));
        return propertiesFactoryBean;
    }
}
