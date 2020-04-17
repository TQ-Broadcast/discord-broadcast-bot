package space.delusive.discord.racoonsuperbot.service;

import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import space.delusive.discord.racoonsuperbot.integration.YoutubeIntegration;
import space.delusive.discord.racoonsuperbot.repository.YoutubeChannelRepository;
import space.delusive.discord.racoonsuperbot.repository.YoutubeVideoRepository;
import space.delusive.discord.racoonsuperbot.discord.DiscordManager;
import space.delusive.discord.racoonsuperbot.domain.YoutubeChannel;
import space.delusive.discord.racoonsuperbot.domain.YoutubeVideo;
import space.delusive.discord.racoonsuperbot.integration.dto.YoutubeVideoDto;

import java.util.Optional;

@Component
@AllArgsConstructor
public class YoutubeService {
    private final YoutubeChannelRepository youtubeChannelRepository;
    private final YoutubeVideoRepository youtubeVideoRepository;
    private final YoutubeIntegration youtubeIntegration;
    private final DiscordManager discordManager;

    @Scheduled(fixedDelay = 30000) // run every 30 seconds
    public void run() {
        Iterable<YoutubeChannel> channels = youtubeChannelRepository.findAll();
        channels.forEach(youtubeChannel -> {
            Optional<YoutubeVideo> newVideo = getNewVideo(youtubeChannel.getUploadsPlaylistId());
            newVideo.ifPresent(youtubeVideo ->
                discordManager.informAboutNewVideoOnYoutube(youtubeChannel, youtubeVideo));
        });
    }

    private Optional<YoutubeVideo> getNewVideo(String playlistId) {
        YoutubeVideoDto lastUploadedVideo = youtubeIntegration.getLastUploadedVideoByPlaylistId(playlistId);
        val optionalVideoFromDb = youtubeVideoRepository.getByYoutubeId(lastUploadedVideo.getVideoId());
        if (optionalVideoFromDb.isEmpty()) { //we don't know about last uploaded video so it is new
            YoutubeVideo youtubeVideo = new YoutubeVideo(lastUploadedVideo.getVideoId());
            youtubeVideoRepository.save(youtubeVideo);
            return Optional.of(youtubeVideo);
        }
        return Optional.empty();
    }
}
