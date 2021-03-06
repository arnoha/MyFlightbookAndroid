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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import Model.Airport;
import Model.ApproachDescription;
import Model.DecimalEdit;

public class ActAddApproach extends Activity {

    // intent keys
    public static final String AIRPORTSFORAPPROACHES = "com.myflightbook.android.AirportsForApproaches";
    public static final String APPROACHDESCRIPTIONRESULT = "com.myflightbook.android.ApproachDescriptionResult";
    public static final String APPROACHADDTOTOTALSRESULT = "com.myflightbook.android.ApproachAddToTotalsResult";
    public static final int APPROACH_DESCRIPTION_REQUEST_CODE = 50382;

    ApproachDescription approachDescription = new ApproachDescription();

    String approachBase = "";
    String approachSuffix = "";
    String runwayBase = "";
    String runwaySuffix = "";
    String[] rgAirports = new String[0];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addapproach);

        // Set up the approach type spinner...
        Spinner s = (Spinner) findViewById(R.id.spnApproachType);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ApproachDescription.ApproachNames);
        adapter.setDropDownViewResource(R.layout.samplequestion);
        s.setAdapter(adapter);
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                approachBase = ApproachDescription.ApproachNames[i];
                approachDescription.approachName = approachBase + approachSuffix;
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                approachBase = "";
                approachDescription.approachName = approachBase + approachSuffix;
            }
        });

        // Then the approach suffix spinner
        s = (Spinner) findViewById(R.id.spnApproachSuffix);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ApproachDescription.ApproachSuffixes);
        adapter.setDropDownViewResource(R.layout.samplequestion);
        s.setAdapter(adapter);
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                approachSuffix = ApproachDescription.ApproachSuffixes[i];
                approachDescription.approachName = approachBase + approachSuffix;
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                approachSuffix = "";
                approachDescription.approachName = approachBase + approachSuffix;
            }
        });

        // Runway spinner...
        s = (Spinner) findViewById(R.id.spnApproachRunway);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ApproachDescription.RunwayNames);
        adapter.setDropDownViewResource(R.layout.samplequestion);
        s.setAdapter(adapter);
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                runwayBase = ApproachDescription.RunwayNames[i];
                approachDescription.runwayName = runwayBase + runwaySuffix;
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                runwayBase = "";
                approachDescription.runwayName = runwayBase + runwaySuffix;
            }
        });

        // And Runway suffix spinner.
        s = (Spinner) findViewById(R.id.spnApproachRunwaySuffix);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ApproachDescription.RunwayModifiers);
        adapter.setDropDownViewResource(R.layout.samplequestion);
        s.setAdapter(adapter);
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                runwaySuffix = ApproachDescription.RunwayModifiers[i];
                approachDescription.runwayName = runwayBase + runwaySuffix;
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                runwaySuffix = "";
                approachDescription.runwayName = runwayBase + runwaySuffix;
            }
        });

        String szAirports = getIntent().getStringExtra(AIRPORTSFORAPPROACHES);
        rgAirports = Airport.SplitCodes(szAirports);

        findViewById(R.id.spnAirport).setVisibility(rgAirports.length > 1 ? View.VISIBLE : View.GONE);
        EditText et = (EditText) findViewById(R.id.txtAirport);
        et.setVisibility(rgAirports.length > 1 ? View.GONE : View.VISIBLE);
        if (rgAirports.length == 1) {
            approachDescription.airportName = rgAirports[0];
            et.setText(rgAirports[0]);
        }

        if (rgAirports.length > 1) {
            s = (Spinner) findViewById(R.id.spnAirport);

            adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, rgAirports);

            adapter.setDropDownViewResource(R.layout.samplequestion);
            s.setAdapter(adapter);

            s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    approachDescription.airportName = rgAirports[i];
                }

                public void onNothingSelected(AdapterView<?> adapterView) {
                    approachDescription.airportName = "";
                }
            });
        }

        CheckBox ckAddToTotals = (CheckBox) findViewById(R.id.ckAddToApproachTotals);
        ckAddToTotals.setChecked(approachDescription.addToApproachCount);
        ckAddToTotals.setOnCheckedChangeListener((buttonView,  isChecked) -> approachDescription.addToApproachCount = isChecked);
    }

    @Override
    public void onBackPressed() {
        approachDescription.approachCount = ((DecimalEdit) findViewById(R.id.txtApproachCount)).getIntValue();
        String szTypedAirports = ((EditText) findViewById(R.id.txtAirport)).getText().toString();
        if (szTypedAirports.length() > 0)
            approachDescription.airportName = szTypedAirports;

        Bundle bundle = new Bundle();
        bundle.putString(APPROACHDESCRIPTIONRESULT, approachDescription.toString());
        bundle.putInt(APPROACHADDTOTOTALSRESULT, approachDescription.addToApproachCount ? approachDescription.approachCount : 0);

        Intent mIntent = new Intent();
        mIntent.putExtras(bundle);
        setResult(RESULT_OK, mIntent);
        super.onBackPressed();
    }
}
