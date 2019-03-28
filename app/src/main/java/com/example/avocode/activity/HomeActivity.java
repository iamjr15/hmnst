package com.example.avocode.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.bumptech.glide.Glide;
import com.example.avocode.R;
import com.example.avocode.fragment.HomeFragment;
import com.example.avocode.utils.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.orm.SchemaGenerator;
import com.orm.SugarContext;
import com.orm.SugarDb;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dbmodel.User;

import static com.example.avocode.config.Constants.FRAGMENT_HOME;
import static com.example.avocode.utils.Util.checkEmptyStrings;

//HomeActivity to show map on screen and navigation to another screens
public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = HomeActivity.class.getSimpleName();
    public int currentFragment = 0;
    private Util util;
    private Bundle bundle = new Bundle();
    @BindView(R.id.imageViewUser)
    ImageView imageViewUser;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer_layout;
    @BindView(R.id.textViewCity)
    TextView textViewCity;
    private String currentCity;
    private String[] capitals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setSelectedFragment(FRAGMENT_HOME, bundle);
        ButterKnife.bind(this);
        imageViewUser.setImageResource(R.drawable.ic_user);
        capitals = getResources().getStringArray(R.array.capitals);
        currentCity = capitals[0];
        initDrawer();
    }

    // TO SET FRAGMENTS ON HomeActivity DYNAMICALLY
    private void setSelectedFragment(int itemId, Bundle bundle) {
        if (currentFragment != itemId) {
            Fragment fragment;
            switch (itemId) {
                case FRAGMENT_HOME:
                    fragment = new HomeFragment();
                    fragment.setArguments(bundle);
                    break;
                default:
                    fragment = new HomeFragment();
                    fragment.setArguments(bundle);
                    break;
            }
            invalidateOptionsMenu();
            try {
                final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content, fragment);
                ft.addToBackStack(fragment.getClass().getName());
                ft.commitAllowingStateLoss();
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        if (currentFragment == FRAGMENT_HOME) {
            finish();
        } else {
            super.onBackPressed();
//            if (getCurrentFragment().equalsIgnoreCase(HomeFragment.class.getName())) {
//                initList();
//            }
        }
    }

    //FETCH PREVIOUS FRAGMENT FROM BACKSTACK
    private String getCurrentFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        return fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();
    }

    // Initialize Drawer and load data
    private void initDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(HomeActivity.this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer_layout.addDrawerListener(toggle);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View view = navigationView.getHeaderView(0);

        //Get  user's profile
        User user = User.findById(User.class, 1);
        if (user != null) {
            if (!checkEmptyStrings(user.uriPath)) {
                Glide.with(this).load(user.uriPath).into((ImageView) view.findViewById(R.id.imageView));
            }
            TextView textViewFullName = view.findViewById(R.id.textViewFullName);
            TextView textViewNumber = view.findViewById(R.id.textViewNumber);
            textViewFullName.setText(String.format("%s %s", user.firstName, user.lastName));
            textViewNumber.setText(user.phone);
            MaterialRippleLayout materialSignOut = view.findViewById(R.id.materialSignOut);
            materialSignOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeDrawer();
                    new AlertDialog.Builder(HomeActivity.this)
                            .setTitle(getString(R.string.attention))
                            .setMessage(getString(R.string.message_sign_out))
                            // Specifying a listener allows you to take an action before dismissing the dialog.
                            // The dialog is automatically dismissed when a dialog button is clicked.
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Continue with sign out operation
                                    try {
                                        FirebaseAuth.getInstance().signOut();
                                        SugarContext.terminate();
                                        SchemaGenerator schemaGenerator = new SchemaGenerator(getApplicationContext());
                                        schemaGenerator.deleteTables(new SugarDb(getApplicationContext()).getDB());
                                        SugarContext.init(getApplicationContext());
                                        schemaGenerator.createDatabase(new SugarDb(getApplicationContext()).getDB());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    startActivity(new Intent(HomeActivity.this, MainActivity.class));
                                    finish();
                                }
                            })

                            // A null listener allows the button to dismiss the dialog and take no further action.
                            .setNegativeButton(android.R.string.no, null)
                            .show();


                }
            });

        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    @OnClick(R.id.imageViewDrawer)
    public void onDrawerClicked() {
        drawer_layout.openDrawer(GravityCompat.START);
    }

    @OnClick(R.id.textViewCity)
    public void onShowCapitals() {
        showCapitals();
    }

    private void closeDrawer() {
        drawer_layout.closeDrawer(GravityCompat.START);
    }

    public void setCity(String city) {
        textViewCity.setText(city);
    }

    private void showCapitals() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(HomeActivity.this);
        builderSingle.setTitle("Select One City");
        final ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(HomeActivity.this, R.array.capitals, android.R.layout.select_dialog_singlechoice);

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                textViewCity.setText(String.valueOf(arrayAdapter.getItem(which)));
            }
        });
        builderSingle.show();
    }

//    private ArrayList createCapitalList() {
//        ArrayList<DropdownListItem> dropdownListItems = new ArrayList<>();
//
//        if (capitals.length > 0) {
//            for (int i = 0; i < capitals.length; i++) {
//                dropdownListItems.add(new DropdownListItem(i, capitals[i]));
//            }
//        }
//        return dropdownListItems;
//    }
}
