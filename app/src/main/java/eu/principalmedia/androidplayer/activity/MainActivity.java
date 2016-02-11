package eu.principalmedia.androidplayer.activity;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

import java.io.IOException;

import eu.principalmedia.androidplayer.entities.Song;
import eu.principalmedia.androidplayer.fragment.AlbumsFragment;
import eu.principalmedia.androidplayer.fragment.ArtistsFragment;
import eu.principalmedia.androidplayer.fragment.FavoritesFragment;
import eu.principalmedia.androidplayer.fragment.GenresFragment;
import eu.principalmedia.androidplayer.fragment.PlayerFragment;
import eu.principalmedia.androidplayer.fragment.PlaylistFragment;
import eu.principalmedia.androidplayer.interfaces.OnTrackListener;
import eu.principalmedia.androidplayer.service.MediaPlayerService;
import eu.principalmedia.androidplayer.R;
import eu.principalmedia.androidplayer.repository.SongRepository;
import eu.principalmedia.androidplayer.fragment.TracksFragment;
import eu.principalmedia.androidplayer.utils.Animations;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnTrackListener, PlayerFragment.PlayerListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    MediaPlayerService mMediaPlayerService;
    FrameLayout fragmentContainer;
    FrameLayout fragmentContainerPlayer;

    SongRepository songRepository;

    TracksFragment tracksFragment;
    PlayerFragment playerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        fragmentContainer = (FrameLayout) findViewById(R.id.fragment_container);
        fragmentContainerPlayer = (FrameLayout) findViewById(R.id.fragment_container_player);

        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        songRepository = new SongRepository(getContentResolver());

        Intent intent = new Intent(this, MediaPlayerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        playerFragment = PlayerFragment.newInstance();

        if (savedInstanceState != null) {
            tracksFragment = (TracksFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            tracksFragment.setRepository(songRepository);
            return;
        }

        tracksFragment = TracksFragment.newInstance(songRepository);
        tracksFragment.setRepository(songRepository);

        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, tracksFragment).commit();
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
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container_player) == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container_player, playerFragment).commit();
        }
    }

    @Override
    public void onPauseMediaPlayer() {
        mMediaPlayerService.pause();
        removePlayerFragment();
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
            mMediaPlayerService.setMediaPlayerListener(playerFragment);
            mMediaPlayerService.setMediaPlayerListener(tracksFragment);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

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
            if (!(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof TracksFragment)) {
                tracksFragment = TracksFragment.newInstance(songRepository);
                tracksFragment.setRepository(songRepository);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, tracksFragment).commit();
            }
        } else if (id == R.id.nav_albums) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, AlbumsFragment.newInstance()).commit();
        } else if (id == R.id.nav_artists) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, ArtistsFragment.newInstance()).commit();
        } else if (id == R.id.nav_genres) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, GenresFragment.newInstance()).commit();
        } else if (id == R.id.nav_playlist) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, PlaylistFragment.newInstance()).commit();
        } else if (id == R.id.nav_favorites) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, FavoritesFragment.newInstance()).commit();
        }else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
