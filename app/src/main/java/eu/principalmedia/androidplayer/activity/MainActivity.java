package eu.principalmedia.androidplayer.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.IOException;

import eu.principalmedia.androidplayer.FragmentUtils;
import eu.principalmedia.androidplayer.entities.Album;
import eu.principalmedia.androidplayer.entities.Artist;
import eu.principalmedia.androidplayer.entities.Song;
import eu.principalmedia.androidplayer.fragment.AlbumsFragment;
import eu.principalmedia.androidplayer.fragment.ArtistsFragment;
import eu.principalmedia.androidplayer.fragment.FavoritesFragment;
import eu.principalmedia.androidplayer.fragment.PlayerFragment;
import eu.principalmedia.androidplayer.fragment.PlaylistFragment;
import eu.principalmedia.androidplayer.fragment.TrackListFragment;
import eu.principalmedia.androidplayer.interfaces.OnTrackListener;
import eu.principalmedia.androidplayer.service.MediaPlayerService;
import eu.principalmedia.androidplayer.R;
import eu.principalmedia.androidplayer.repository.SongRepository;
import eu.principalmedia.androidplayer.utils.Animations;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnTrackListener, PlayerFragment.PlayerListener, AlbumsFragment.AlbumListener,
        ArtistsFragment.ArtistsListener{

    public static final String TAG = MainActivity.class.getSimpleName();
    public Toolbar mToolbar;

    public MediaPlayerService mMediaPlayerService;
    FrameLayout fragmentContainer;
    FrameLayout fragmentContainerPlayer;
    ToggleButton playerHideShowButton;

    SongRepository songRepository;

//    TrackListFragment tracksFragment;
    PlayerFragment playerFragment;
//    AlbumsFragment albumsFragment;

    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Animation rotateAnim = AnimationUtils.loadAnimation(this, R.anim.button_rotate);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        fragmentContainer = (FrameLayout) findViewById(R.id.fragment_container);
        fragmentContainerPlayer = (FrameLayout) findViewById(R.id.fragment_container_player);
        playerHideShowButton = (ToggleButton) findViewById(R.id.player_hide_show_button);
        playerHideShowButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    if (isChecked) {
                        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_player, playerFragment).commit();
                    } else {
                        removePlayerFragment();
                    }
                    playerHideShowButton.startAnimation(rotateAnim);
                }
            }
        });

        adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView.setVisibility(View.VISIBLE);
            }
        });

        setSupportActionBar(mToolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        Intent intent = new Intent(this, MediaPlayerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

//        if (savedInstanceState != null) {
//            tracksFragment = (TracksFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
//        }

    }

    private void addFragments(SongRepository songRepository) {
        this.songRepository = songRepository;

        playerFragment = PlayerFragment.newInstance();
        playerFragment.setSongRepository(songRepository);

        TrackListFragment tracksFragment = TrackListFragment.newInstance(TrackListFragment.TRACKS);
        tracksFragment.setRepository(songRepository);

        AlbumsFragment albumsFragment = AlbumsFragment.newInstance();
        albumsFragment.setRepository(songRepository);

        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, tracksFragment).commit();
//        if (mMediaPlayerService.isPlaying()) {

            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_player, playerFragment).commit();
            playerHideShowButton.setChecked(true);

//        }

        playerFragment.setService(mMediaPlayerService);
        tracksFragment.setService(mMediaPlayerService);
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        unbindService(mConnection);
        super.onDestroy();
    }

    @Override
    public void onPlayMediaPlayer(Song song) {
        try {
            mMediaPlayerService.play(song);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAddPlayerFragment() {
//        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container_player) == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container_player, playerFragment).commit();
//        }
    }

    @Override
    public void onAddRemovePlayerFragment() {
//        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container_player) == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container_player, playerFragment).commit();
//        } else {
//            removePlayerFragment();
//        }
    }

    @Override
    public void onPauseMediaPlayer() {
        mMediaPlayerService.pause();
//        removePlayerFragment();
    }

    @Override
    public void onSeekChanged(int progress) {
        mMediaPlayerService.setProgress(progress);
    }

    private void removePlayerFragment() {
        Animation.AnimationListener animationListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                getSupportFragmentManager().beginTransaction()
                        .remove(playerFragment).commit();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };
        Animations.collapse(fragmentContainerPlayer, animationListener);
    }

    @Override
    public void onViewCreatedPlayerFragment() {
        Animations.expand(fragmentContainerPlayer);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMediaPlayerService = ((MediaPlayerService.MediaPlayerBinder) service).getService();

            if (mMediaPlayerService.getSongRepository() != null) {
                addFragments(mMediaPlayerService.getSongRepository());
            } else {
                songRepository = new SongRepository(getContentResolver());
                mMediaPlayerService.setSongRepository(songRepository);
                addFragments(songRepository);
            }

//            mMediaPlayerService.setMediaPlayerListener(playerFragment);
//            mMediaPlayerService.setMediaPlayerListener(tracksFragment);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "Service Disconnected");
        }
    };

    @Override
    public void onAlbumClick(Album album) {
        TrackListFragment trackListFragment = TrackListFragment.newInstance(TrackListFragment.ALBUMS, album.getAlbumId());
        trackListFragment.setRepository(songRepository);
        trackListFragment.setService(mMediaPlayerService);

        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_right, R.anim.exit_left, R.anim.enter_left, R.anim.exit_right)
                .replace(R.id.fragment_container, trackListFragment)
                .addToBackStack(null).commit();
    }

    @Override
    public void onArtistClick(Artist artist) {
        TrackListFragment trackListFragment = TrackListFragment.newInstance(TrackListFragment.ARTISTS, artist.getArtistId());
        trackListFragment.setRepository(songRepository);
        trackListFragment.setService(mMediaPlayerService);

        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_right, R.anim.exit_left, R.anim.enter_left, R.anim.exit_right)
                .replace(R.id.fragment_container, trackListFragment)
                .addToBackStack(null).commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_tracks) {
            if (!(getSupportFragmentManager().findFragmentById(R.id.fragment_container).getClass() == TrackListFragment.class)) {
                TrackListFragment tracksFragment = TrackListFragment.newInstance(TrackListFragment.TRACKS);
                tracksFragment.setRepository(songRepository);
                tracksFragment.setService(mMediaPlayerService);

//                    getSupportFragmentManager().popBackStack();
                clearBackStack();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, tracksFragment).commit();
            } else {
                if (!((TrackListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container)).type.equals(TrackListFragment.TRACKS)) {
                    TrackListFragment tracksFragment = TrackListFragment.newInstance(TrackListFragment.TRACKS);
                    tracksFragment.setRepository(songRepository);
                    tracksFragment.setService(mMediaPlayerService);

//                    getSupportFragmentManager().popBackStack();
                    clearBackStack();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, tracksFragment).commit();
                }
            }
        } else if (id == R.id.nav_albums) {
            if (!(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof AlbumsFragment)) {
                AlbumsFragment albumsFragment = AlbumsFragment.newInstance();
                albumsFragment.setRepository(songRepository);

//                getSupportFragmentManager().popBackStack();
                clearBackStack();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, albumsFragment).commit();
            }
        } else if (id == R.id.nav_artists) {
            if (!(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof ArtistsFragment)) {
                ArtistsFragment artistsFragment = ArtistsFragment.newInstance();
                artistsFragment.setRepository(songRepository);

//                getSupportFragmentManager().popBackStack();
                clearBackStack();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, artistsFragment).commit();
            }
        } /*else if (id == R.id.nav_genres) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, GenresFragment.newInstance()).commit();
        }*/ else if (id == R.id.nav_playlist) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, PlaylistFragment.newInstance()).commit();
        } else if (id == R.id.nav_favorites) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, FavoritesFragment.newInstance()).commit();
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void clearBackStack() {
        FragmentUtils.sDisableFragmentAnimations = true;
        getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentUtils.sDisableFragmentAnimations = false;
    }

}
