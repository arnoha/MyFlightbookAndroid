/*
	MyFlightbook for Android - provides native access to MyFlightbook
	pilot's logbook
    Copyright (C) 2017 MyFlightbook, LLC

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.myflightbook.android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleExpandableListAdapter;

import com.myflightbook.android.WebServices.AuthToken;
import com.myflightbook.android.WebServices.MFBSoap;
import com.myflightbook.android.WebServices.VisitedAirportSvc;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

import Model.MFBUtil;
import Model.VisitedAirport;

public class ActVisitedAirports extends ExpandableListFragment implements MFBMain.Invalidatable {

    private static VisitedAirport[] visitedAirports = null;

    private class SoapTask extends AsyncTask<Void, Void, MFBSoap> {
        private Context m_Context = null;
        private ProgressDialog m_pd = null;
        Object m_Result = null;

        SoapTask(Context c) {
            super();
            m_Context = c;
        }

        @Override
        protected MFBSoap doInBackground(Void... params) {
            VisitedAirportSvc vas = new VisitedAirportSvc();
            m_Result = vas.VisitedAirportsForUser(AuthToken.m_szAuthToken, m_Context);
            return vas;
        }

        protected void onPreExecute() {
            m_pd = MFBUtil.ShowProgress(m_Context, m_Context.getString(R.string.prgVisitedAirports));
        }

        protected void onPostExecute(MFBSoap svc) {
            if (!isAdded() || getActivity().isFinishing())
                return;

            ActVisitedAirports.visitedAirports = (VisitedAirport[]) m_Result;

            if (ActVisitedAirports.visitedAirports == null || svc.getLastError().length() > 0)
                MFBUtil.Alert(m_Context, getString(R.string.txtError), svc.getLastError());
            else
                ActVisitedAirports.this.populateList();
            try {
                m_pd.dismiss();
            } catch (Exception ignored) {
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.setHasOptionsMenu(true);

        return inflater.inflate(R.layout.expandablelist, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MFBMain.registerNotifyDataChange(this);
        MFBMain.registerNotifyResetAll(this);
    }

    public void onResume() {
        super.onResume();
        if (visitedAirports == null)
            new SoapTask(getActivity()).execute();
        else
            populateList();
    }

    @Override
    // reuse currency menu here.
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.currencymenu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menuRefresh:
                new SoapTask(getActivity()).execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void populateList() {
        if (visitedAirports == null || visitedAirports.length == 0)
            return;

        // This maps the headers to the individual sub-lists.
        HashMap<String, HashMap<String, String>> headers = new HashMap<>();
        HashMap<String, ArrayList<HashMap<String, String>>> childrenMaps = new HashMap<>();

        // Keep a list of the keys in order
        ArrayList<String> alKeys = new ArrayList<>();

        Arrays.sort(visitedAirports);

        // First do "All airports"
        String szKeyLast = getString(R.string.AllAirports);
        alKeys.add(szKeyLast);
        HashMap<String, String> hmAllAirports = new HashMap<>();
        hmAllAirports.put("sectionName", szKeyLast);
        headers.put(szKeyLast, hmAllAirports);

        hmAllAirports = new HashMap<>();
        hmAllAirports.put("Code", "");
        hmAllAirports.put("Name", szKeyLast);
        int cVisits = visitedAirports.length;
        hmAllAirports.put("Visits", getResources().getQuantityString(R.plurals.uniqueAirportsCount, cVisits, cVisits));
        hmAllAirports.put("Distance", "");
        hmAllAirports.put("Position", "-1");
        ArrayList<HashMap<String, String>> alAllAirports = new ArrayList<>();
        alAllAirports.add(hmAllAirports);
        childrenMaps.put(szKeyLast, alAllAirports);

        // slice and dice into headers/first names
        for (int i = 0; i < visitedAirports.length; i++) {
            VisitedAirport va = visitedAirports[i];

            // get the first letter for this property as the grouping key
            String szKey = va.airport.FacilityName.substring(0, 1).toUpperCase(Locale.getDefault());
            if (szKey.compareTo(szKeyLast) != 0) {
                alKeys.add(szKey);
                szKeyLast = szKey;
            }

            HashMap<String, String> hmGroups = (headers.containsKey(szKey) ? headers.get(szKey) : new HashMap<>());
            hmGroups.put("sectionName", szKey);
            headers.put(szKey, hmGroups);

            // Get the array-list for that key, creating it if necessary
            ArrayList<HashMap<String, String>> alAirports;
            alAirports = (childrenMaps.containsKey(szKey) ? childrenMaps.get(szKey) : new ArrayList<>());

            HashMap<String, String> hmProperty = new HashMap<>();
            String szDistance = va.airport.Distance > 0 ? String.format(Locale.getDefault(), "(%.1fnm) ", va.airport.Distance) : "";
            String szEarliestVisit = DateFormat.getDateInstance(DateFormat.SHORT).format(va.EarliestDate);
            String szLatestVisit = DateFormat.getDateInstance(DateFormat.SHORT).format(va.LatestDate);
            String szVisits = (va.NumberOfVisits > 1) ?
                    String.format(getString(R.string.vaMultiVisit), va.NumberOfVisits, szEarliestVisit, szLatestVisit) :
                    String.format(getString(R.string.vaSingleVisit), szEarliestVisit);
            hmProperty.put("Code", String.format("%s - ", va.Code));
            hmProperty.put("Name", va.airport.FacilityName);
            hmProperty.put("Distance", szDistance);
            hmProperty.put("Visits", szVisits);
            hmProperty.put("Position", String.format(Locale.US, "%d", i));
            alAirports.add(hmProperty);

            childrenMaps.put(szKey, alAirports);
        }

        // put the above into arrayLists, but in the order that the keys were encountered.  .values() is an undefined order.
        ArrayList<HashMap<String, String>> headerList = new ArrayList<>();
        ArrayList<ArrayList<HashMap<String, String>>> childrenList = new ArrayList<>();
        for (String s : alKeys) {
            headerList.add(headers.get(s));
            childrenList.add(childrenMaps.get(s));
        }

        final SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
                getActivity(),
                headerList,
                R.layout.grouprow,
                new String[]{"sectionName"},
                new int[]{R.id.propertyGroup},
                childrenList,
                R.layout.visitedairportitem,
                new String[]{"Code", "Name", "Distance", "Visits"},
                new int[]{R.id.txtCode, R.id.txtName, R.id.txtDistance, R.id.txtVisits}
        );
        setListAdapter(adapter);

        getExpandableListView().setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {

            if (!MFBMain.HasMaps())
                return false;

            @SuppressWarnings("unchecked")
            HashMap<String, String> hmProp = (HashMap<String, String>) adapter.getChild(groupPosition, childPosition);
            int position = Integer.parseInt(hmProp.get("Position"));
            String szRoute = "";
            String szAlias = "";
            if (position < 0) // all airports
                szRoute = VisitedAirport.toRoute(visitedAirports);
            else if (position >= 0 && position < visitedAirports.length) {
                szRoute = visitedAirports[position].Code;
                szAlias = visitedAirports[position].Aliases;
            }

            Intent i = new Intent(ActVisitedAirports.this.getActivity(), ActFlightMap.class);
            i.putExtra(ActFlightMap.ROUTEFORFLIGHT, szRoute);
            i.putExtra(ActFlightMap.EXISTINGFLIGHTID, -1);
            i.putExtra(ActFlightMap.PENDINGFLIGHTID, -1);
            i.putExtra(ActFlightMap.ALIASES, szAlias);
            startActivity(i);

            return false;
        });
    }

    public void invalidate() {
        visitedAirports = null;
    }
}
