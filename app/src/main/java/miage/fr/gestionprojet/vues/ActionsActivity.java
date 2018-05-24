package miage.fr.gestionprojet.vues;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import miage.fr.gestionprojet.R;
import miage.fr.gestionprojet.adapters.ActionClicked;
import miage.fr.gestionprojet.adapters.ActionsAdapter;
import miage.fr.gestionprojet.models.Action;
import miage.fr.gestionprojet.models.Domaine;
import miage.fr.gestionprojet.models.dao.DaoAction;
import miage.fr.gestionprojet.outils.Constants;
import miage.fr.gestionprojet.outils.DividerItemDecoration;
import miage.fr.gestionprojet.outils.Outils;

public class ActionsActivity extends AppCompatActivity implements ActionClicked {
    private String initial;

    @BindView(R.id.actionRecycler)
    RecyclerView mRecyclerView;

    @BindView(R.id.year_plus)
    ImageButton yearPlus;

    @BindView(R.id.year_minus)
    ImageButton yearMinus;

    @BindView(R.id.week_plus)
    ImageButton weekPlus;

    @BindView(R.id.week_minus)
    ImageButton weekMinus;

    @BindView(R.id.edit_text_year)
    EditText yearEditText;

    @BindView(R.id.edit_text_week)
    EditText weekEditText;

    @BindView(R.id.emptyView)
    TextView mEmptyView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;


    private int year;
    private int week;
    private Date dateSaisie;
    private long idProjet;

    public static final  String EXTRA_INITIAL = "initial";
    public static final String EXTRA_PROJET = "projet visu";
    private static final String TAG = "[ActionsActivity]";
    private static final String TYPE = "type";
    private static final String PHASE = "phase";
    private static final String DOMAINE = "domaine";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actions);
        ButterKnife.bind(this);

        try {
            initial = getIntent().getStringExtra(DetailsProjetActivity.EXTRA_INITIAL);
            idProjet = getIntent().getLongExtra(EXTRA_PROJET,0);
        } catch(Exception e) {
            Log.e(TAG, e.toString());
            finish();
        }

        Calendar c = Calendar.getInstance();
        week = c.get(Calendar.WEEK_OF_YEAR);
        year = c.get(Calendar.YEAR);
        if (toolbar!=null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this));
        weekEditText.setText(String.valueOf(week));
        yearEditText.setText(String.valueOf(year));
        yearEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    year = Integer.parseInt(editable.toString());
                    dateSaisie = Outils.weekOfYearToDate(year,week);
                    refreshAdapter(DaoAction.loadActionsByDate(dateSaisie, idProjet));
                } catch(Exception e) {
                    Log.e(TAG, e.toString());
                    yearEditText.setError("");
                }
            }
        });

        weekEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    week = Integer.parseInt(editable.toString());
                    dateSaisie = Outils.weekOfYearToDate(year,week);
                    refreshAdapter(DaoAction.loadActionsByDate(dateSaisie, idProjet));
                } catch(Exception e) {
                    Log.e(TAG, e.toString());
                    weekEditText.setError("");
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        dateSaisie = Outils.weekOfYearToDate(year,week);
        refreshAdapter(DaoAction.loadActionsByDate(dateSaisie, idProjet));
    }

    private void refreshAdapter(List<Action> actions){
        if (!actions.isEmpty()) {
            mEmptyView.setVisibility(View.INVISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);
            ActionsAdapter adapter = new ActionsAdapter(actions);
            adapter.setmListener(this);
            mRecyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    private void resetErrors() {
        yearEditText.setError(null);
        weekEditText.setError(null);
    }

    @OnClick(R.id.week_minus)
    public void onWeekMinusClick() {
        resetErrors();
        week = Integer.parseInt(weekEditText.getText().toString()) - 1;
        weekEditText.setText(String.valueOf(week));
        if (week == 0) {
            week = 52;
            year--;
        }
        weekEditText.setText(String.valueOf(week));
        yearEditText.setText(String.valueOf(year));
        dateSaisie = Outils.weekOfYearToDate(year,week);
        refreshAdapter(DaoAction.loadActionsByDate(dateSaisie,idProjet));
    }

    @OnClick(R.id.week_plus)
    public void onWeekPlusClick() {
        resetErrors();
        week = Integer.parseInt(weekEditText.getText().toString()) + 1;
        if (week > 52) {
            week = 1;
            year++;
        }
        weekEditText.setText(String.valueOf(week));
        yearEditText.setText(String.valueOf(year));
        dateSaisie = Outils.weekOfYearToDate(year,week);
        refreshAdapter(DaoAction.loadActionsByDate(dateSaisie,idProjet));
    }

    @OnClick(R.id.year_minus)
    public void onYearMinusClick() {
        resetErrors();
        year = Integer.parseInt(yearEditText.getText().toString()) - 1;
        dateSaisie = Outils.weekOfYearToDate(year,week);
        if (year < 2000) {
            yearEditText.setError("");
            return;
        }
        yearEditText.setText(String.valueOf(year));
        refreshAdapter(DaoAction.loadActionsByDate(dateSaisie,idProjet));
    }

    @OnClick(R.id.year_plus)
    public void onYearPlusClick() {
        resetErrors();
        year = Integer.parseInt(yearEditText.getText().toString()) + 1;
        dateSaisie = Outils.weekOfYearToDate(year,week);
        yearEditText.setText(String.valueOf(year));
        refreshAdapter(DaoAction.loadActionsByDate(dateSaisie,idProjet));
    }

    @Override
    public void selectedAction(Action action) {
        showDialog(action);
    }

    private void showDialog(final Action action){
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_details_action, null, false);
        TextView phase = (TextView) layout.findViewById(R.id.phase);
        TextView name = (TextView) layout.findViewById(R.id.name);
        TextView dtDebut = (TextView) layout.findViewById(R.id.dtDebut);
        TextView dtFin = (TextView) layout.findViewById(R.id.dtFin);
        TextView nbJr = (TextView) layout.findViewById(R.id.nbJr);
        TextView estimation = (TextView) layout.findViewById(R.id.estimation);
        phase.setText(action.getPhase());
        name.setText(action.getCode());
        String dateDebut = "Date Debut : "+ new SimpleDateFormat(Constants.DATE_FORMAT).format(action.getDtDeb());
        dtDebut.setText(dateDebut);
        String dateFin = "Date Fin Prevue: "+ new SimpleDateFormat(Constants.DATE_FORMAT).format(action.getDtFinPrevue());
        dtFin.setText(dateFin);
        String nombreJours = "Nombre de jours prevus : " + action.getNbJoursPrevus();
        nbJr.setText(nombreJours);
        String estimationText = "Cout par jour : " + action.getCoutParJour();
        estimation.setText(estimationText);
        final PopupWindow popup = new PopupWindow(layout,800,800, true);
        popup.setOutsideTouchable(true);
        popup.setTouchable(true);
        Drawable drawable = ContextCompat.getDrawable(ActionsActivity.this, R.drawable.background_ounded_border);
        popup.setBackgroundDrawable(drawable);
        popup.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popup.dismiss();
                return true;
            }
        });
        popup.showAtLocation(mRecyclerView, Gravity.CENTER, 0, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionmenu, menu);
        getMenuInflater().inflate(R.menu.initial_utilisateur, menu);
        menu.findItem(R.id.initial_utilisateur).setTitle(initial);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nom:
                refreshAdapter(DaoAction.loadActionsOrderByNomAndDate(dateSaisie,idProjet));
                return true;

            case R.id.type:
                showPopUp(TYPE);
                return true;

            case R.id.domain:
                showPopUp(DOMAINE);
                return true;

            case R.id.phase:
                showPopUp(PHASE);
                return true;

            case R.id.initial_utilisateur:
                return true;

            case R.id.charger_donnees:
                Intent intent = new Intent(ActionsActivity.this, ChargementDonneesActivity.class);
                intent.putExtra(EXTRA_INITIAL, (initial));
                startActivity(intent);
                return true;

            case R.id.envoyer_mail:
                Intent intentSendMail = new Intent(ActionsActivity.this, SendMailActivity.class);
                startActivity(intentSendMail);
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showPopUp(String cat){
        ContextThemeWrapper wrapper = new ContextThemeWrapper(this, R.style.MyPopupMenu);
        PopupMenu popupMenu = new PopupMenu(wrapper, mEmptyView);
        Menu menu = popupMenu.getMenu();
        if (TYPE.equalsIgnoreCase(cat)) {
            manageTypePopup(popupMenu, menu);
        } else if (PHASE.equalsIgnoreCase(cat)) {
            managePhasePopup(popupMenu, menu);
        } else if (DOMAINE.equalsIgnoreCase(cat)) {
            manageDomainePopup(popupMenu, menu);
        }
    }

    private void manageTypePopup(PopupMenu popupMenu, Menu menu) {
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_type, menu);
        popupMenu.setGravity(Gravity.CENTER);
        ArrayList<String> types = getTypeTravailAffiche();
        for (int i = 0; i < types.size(); i++) {
            menu.add(0, i, 0, types.get(i));
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.all) {
                    refreshAdapter(DaoAction.loadActionsByDate(dateSaisie,idProjet));
                } else {
                    refreshAdapter(DaoAction.loadActionsByDateAndType(dateSaisie, item.getTitle().toString(),idProjet));
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private void managePhasePopup(PopupMenu popupMenu, Menu menu) {
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_phase, menu);
        popupMenu.setGravity(Gravity.CENTER);
        ArrayList<String> phases = getPhasesAffiches();
        for(int i = 0; i < phases.size(); i++){
            menu.add(0, i, 0, phases.get(i));
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.all) {
                    refreshAdapter(DaoAction.loadActionsByDate(dateSaisie,idProjet));
                } else {
                    refreshAdapter(DaoAction.loadActionsByPhaseAndDate(item.getTitle().toString(),dateSaisie,idProjet));
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private void manageDomainePopup(PopupMenu popupMenu, Menu menu) {
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_domaine, menu);
        popupMenu.setGravity(Gravity.CENTER);
        ArrayList<Domaine> domaines = getDomainesAffiches();
        for(Domaine domaine : domaines){
            menu.add(0,(int)(long) domaine.getId(), 0, domaine.getNom());
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.all) {
                    refreshAdapter(DaoAction.loadActionsByDate(dateSaisie,idProjet));
                } else {
                    refreshAdapter(DaoAction.loadActionsByDomaineAndDate(item.getItemId(),dateSaisie,idProjet));
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private ArrayList<String> getTypeTravailAffiche(){
        ArrayList<String> result = new ArrayList<>();
        List<Action> actions = DaoAction.loadActionsByDate(dateSaisie,idProjet);
        for(Action action : actions){
            if (result.indexOf(action.getTypeTravail()) < 0) {
                result.add(action.getTypeTravail());
            }
        }
        return result;
    }

    private ArrayList<String> getPhasesAffiches(){
        ArrayList<String> result = new ArrayList<>();
        List<Action> actions = DaoAction.loadActionsByDate(dateSaisie,idProjet);
        for(Action action : actions){
            if (result.indexOf(action.getPhase()) < 0) {
                result.add(action.getPhase());
            }
        }
        return result;
    }

    private ArrayList<Domaine> getDomainesAffiches() {
        ArrayList<Domaine> result = new ArrayList<>();
        List<Action> actions = DaoAction.loadActionsByDate(dateSaisie,idProjet);
        for(Action action : actions){
            if (result.indexOf(action.getDomaine()) < 0) {
                result.add(action.getDomaine());
            }
        }
        return result;
    }
}
