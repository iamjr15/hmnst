package com.example.avocode.models

import com.google.android.gms.maps.model.LatLng

class FamilyMemberData(val fullName: String, val avatarUrl: String, val latLng: LatLng? = null, val sharedMessage: String? = null)