import pytube.exceptions
import spotipy
from spotipy.oauth2 import SpotifyClientCredentials
from youtubesearchpython import VideosSearch
from pytube import YouTube
import os
import shutil


# using spotipy
def get_playlist_name(sp, playlist):
    return sp.playlist(playlist)["name"]


def get_names_list(sp, playlist):
    # get the tracks items
    results = sp.playlist_tracks(playlist)
    tracks = results["items"]
    while results["next"]:
        results = sp.next(results)
        tracks.extend(results["items"])

    # load to dictionary
    song_artist_d = dict()

    for track in tracks:
        track_name = track["track"]["name"]
        artist_name = ", ".join([artist["name"] for artist in track["track"]["artists"]])
        # get song duration
        duration_seconds = track["track"]["duration_ms"] / 1000
        seconds_num = int(duration_seconds % 60)
        if len(str(seconds_num)) == 1:
            seconds_num = f"0{seconds_num}"
        duration_formated = f"{int(duration_seconds / 60)}:{seconds_num}"
        #print(f"{track_name} - {artist_name}")
        song_artist_d[track_name] = [artist_name, duration_formated]

    return song_artist_d


def get_names(playlist_link, action):
    client_id = "3095cef11f7a4b8681759c1584dd83f8"
    client_secret = "0be2463d4d9f460085871bb0f0447c69"
    playlist_id = playlist_link.split("/")[-1].split("?")[0]

    client_credentials_manager = SpotifyClientCredentials(client_id=client_id, client_secret=client_secret)
    sp = spotipy.Spotify(client_credentials_manager=client_credentials_manager)

    if action == "pl_name":
        return get_playlist_name(sp, playlist_id)
    elif action == "songs":
        return get_names_list(sp, playlist_id)


# downloading and searching youtube
def download(song, artist, directory):
    # get the link
    song = song.encode("ascii", errors="replace").decode("ascii")
    search_result = VideosSearch(f"{song} {artist}", limit=1).result()
    if search_result and len(search_result) > 0:
        video_link = search_result["result"][0]["link"].encode("ascii", errors="replace").decode("ascii")
    else:
        return ""

    # download

    yt = YouTube(video_link)
    try:
        output_file = yt.streams.get_audio_only(subtype="mp4").download(directory)
        base_file = os.path.splitext(output_file)[0]
        new_file = base_file + ".mp3"
        os.rename(output_file, new_file)
        #shutil.move(new_file, "/storage/emulated/0/Download/")
    except pytube.exceptions.AgeRestrictedError:
        print("age restricted, cannot download")
        return ""

    return new_file



