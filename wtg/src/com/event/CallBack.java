package com.event;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CallBack implements Serializable{
	public static Map<String, List<String>> dialogToCallBacks = new HashMap<String, List<String>>();
	public static Map<String, List<String>> viewToCallBacks = new HashMap<String, List<String>>();
	public static Map<String, List<String>> asyncToRunnable = new HashMap<String, List<String>>();
	public static Set<String> viewRegistars;
	public static Set<String> dialogRegistars;
	public static Set<String> serViceRegistars;
	
	public static Set<String> callBackListener = new HashSet<>();
	//android.view.View: 1-10 
	//1. onKey
	public static final String SETONKEYLISTENER = "void setOnKeyListener(android.view.View$OnKeyListener)";
	public static final String ONKEYLISTENER = "android.view.View$OnKeyListener";
	public static final String ONKEY = "boolean onKey(android.view.View,int,android.view.KeyEvent)";
	public static final String[] array1 = {ONKEY};
	
	//2. onTouch
	public static final String SETONTOUCHLISTENER = "void setOnTouchListener(android.view.View$OnTouchListener)";
	public static final String ONTOUCHLISTENER = "android.view.View$OnTouchListener";
	public static final String ONTOUCH = "boolean onTouch(android.view.View,android.view.MotionEvent)";
	public static final String[] array2 = {ONTOUCH};
	
	//3. onHover
	public static final String SETONHOVERLISTENER = "void setOnHoverListener(android.view.View$OnHoverListener)";
	public static final String ONHOVERLISTENER = "android.view.View$OnHoverListener";
	public static final String ONHOVER = "boolean onHover(android.view.View,android.view.MotionEvent)";
	public static final String[] array3 = {ONHOVER};
	
	//4. onGenericMotion
	public static final String SETONGENERICMOTIONLISTENER = "void setOnGenericMotionListener(android.view.View$OnGenericMotionListener)";
	public static final String ONGENERICMOTIONLISTENER = "android.view.View$OnGenericMotionListener";
	public static final String ONGENERICMOTION = "boolean onGenericMotion(android.view.View,android.view.MotionEvent)";
	public static final String[] array4 = {ONGENERICMOTION};
	
	//5. onLongClick
	public static final String SETONLONGCLICKLISTENER = "void setOnLongClickListener(android.view.View$OnLongClickListener)";
	public static final String ONLONGCLICKLISTENER = "android.view.View$OnLongClickListener";
	public static final String ONLONGCLICK = "boolean onLongClick(android.view.View)";
	public static final String[] array5 = {ONLONGCLICK};
	
	//6. onDrag
	public static final String SETONDRAGLISTENER = "void setOnDragListener(android.view.View$OnDragListener)";
	public static final String ONDRAGLISTENER = "android.view.View$OnDragListener";
	public static final String ONDRAG = "boolean onDrag(android.view.View,android.view.DragEvent)";
	public static final String[] array6 = {ONDRAG};
	
	//7. onFocusChange
	public static final String SETONFOCUSCHANGELISTENER = "void setOnFocusChangeListener(android.view.View$OnFocusChangeListener)";
	public static final String ONFOCUSCHANGELISTENER = "android.view.View$OnFocusChangeListener";
	public static final String ONFOCUSCHANGE = "void onFocusChange(android.view.View,boolean)";
	public static final String[] array7 = {ONFOCUSCHANGE};
	
	//8. onClick
	public static final String SETONCLICKLISTENER = "void setOnClickListener(android.view.View$OnClickListener)";
	public static final String ONCLICKLISTENER = "android.view.View$OnClickListener";
	public static final String ONCLICK = "void onClick(android.view.View)";
	public static final String[] array8 = {ONCLICK};
	
	//9. onCreateContextMenuListener
	public static final String SETONCREATECONTEXTMENULISTENER = "void setOnCreateContextMenuListener(android.view.View$OnCreateContextMenuListener)";
	public static final String ONCREATECONTEXTMENULISTENER = "android.view.View$OnCreateContextMenuListener";
	public static final String ONCONTEXTITEMSELECTED = "boolean onContextItemSelected(android.view.MenuItem)";
	public static final String ONCREATECONTEXTMENU = "void onCreateContextMenu(android.view.ContextMenu,android.view.View,android.view.ContextMenu$ContextMenuInfo)";
	public static final String[] array9 = {ONCREATECONTEXTMENU};
	
	//10. onSystemUiVisibilityChangeListener
	public static final String SETONSYSTEMUIVISIBILITYCHANGELISTENER = "void setOnSystemUiVisibilityChangeListener(android.view.View$OnSystemUiVisibilityChangeListener)";
	public static final String ONSYSTEMUIVISIBILITYCHANGELISTENER = "android.view.View$OnSystemUiVisibilityChangeListener";
	public static final String ONSYSTEMUIVISIBILITYCHANGE = "void onSystemUiVisibilityChange(int)";
	public static final String[] array10 = {ONSYSTEMUIVISIBILITYCHANGE};
	
	//android.preference.PreferenceActivity:11
	//11. onPreferenceTreeClick
	public static final String SETONPREFERENCECLICKLISTENER = "void setOnPreferenceClickListener(android.preference.Preference$OnPreferenceClickListener)";
	public static final String ONPREFERENCECLICKLISTENER = "android.preference.Preference$OnPreferenceClickListener";
	public static final String ONPREFERENCETREECLICK = "boolean onPreferenceTreeClick(android.preference.PreferenceScreen,android.preference.Preference)";
	public static final String[] array11 = {ONPREFERENCETREECLICK};
	
	//android.widget.TextView:12
	//12. OnEditorActionListener
	public static final String SETONEDITORACTIONLISTENER = "void setOnEditorActionListener(android.widget.TextView$OnEditorActionListener)";
	public static final String ONEDITORACTIONLISTENER = "android.widget.TextView$OnEditorActionListener";
	public static final String ONEDITORACTION = "boolean onEditorAction(android.widget.TextView,int,android.view.KeyEvent)";
	public static final String[] array12 = {ONEDITORACTION};
	
	//android.view.ViewGroup:13
	//13. OnHierarchyChangeListener
	public static final String SETONHIERARCHYCHANGELISTENER = "void setOnHierarchyChangeListener(android.view.ViewGroup$OnHierarchyChangeListener)";
	public static final String ONHIERARCHYCHANGELISTENER = "android.view.ViewGroup$OnHierarchyChangeListener";
	public static final String ONCHILDVIEWADDED = "void onChildViewAdded(android.view.View,android.view.View)";
	public static final String ONCHILDVIEWREMOVED = "void onChildViewRemoved(android.view.View,android.view.View)";
	public static final String[] array13 = {ONCHILDVIEWADDED,ONCHILDVIEWREMOVED};
	
	//android.widget.AdapterView:14-16
	//14. OnItemClickListener
	public static final String SETONITEMCLICKLISTENER = "void setOnItemClickListener(android.widget.AdapterView$OnItemClickListener)";
	public static final String ONITEMCLICKLISTENER = "android.widget.AdapterView$OnItemClickListener";
	public static final String ONITEMCLICK = "void onItemClick(android.widget.AdapterView,android.view.View,int,long)";
	public static final String[] array14 = {ONITEMCLICK};
	
	//15. OnItemLongClickListener
	public static final String SETONITEMLONGCLICKLISTENER = "void setOnItemLongClickListener(android.widget.AdapterView$OnItemLongClickListener)";
	public static final String ONITEMLONGCLICKLISTENER = "android.widget.AdapterView$OnItemLongClickListener";
	public static final String ONITEMLONGCLICK = "boolean onItemLongClick(android.widget.AdapterView,android.view.View,int,long)";
	public static final String[] array15 = {ONITEMLONGCLICK};
	
	//16. OnItemSelectedListener
	public static final String SETONITEMSELECTEDLISTENER = "void setOnItemSelectedListener(android.widget.AdapterView$OnItemSelectedListener)";
	public static final String ONITEMSELECTEDLISTENER = "android.widget.AdapterView$OnItemSelectedListener";
	public static final String ONITEMSELECTED = "void onItemSelected(android.widget.AdapterView,android.view.View,int,long)";
	public static final String ONNOTHINGSELECTED = "void onNothingSelected(android.widget.AdapterView)";
	public static final String[] array16 = {ONITEMSELECTED,ONNOTHINGSELECTED};
	
	//android.widget.SeekBar:17
	//17. OnSeekBarChangeListener
	public static final String setOnSeekBarChangeListener = "void setOnSeekBarChangeListener(android.widget.SeekBar$OnSeekBarChangeListener)";
	public static final String OnSeekBarChangeListener = "android.widget.SeekBar$OnSeekBarChangeListener";
	public static final String onProgressChanged = "void onProgressChanged(android.widget.SeekBar,int,boolean)";
	public static final String onStartTrackingTouch = "void onStartTrackingTouch(android.widget.SeekBar)";
	public static final String onStopTrackingTouch = "void onStopTrackingTouch(android.widget.SeekBar)";
	public static final String[] array17 = {onProgressChanged,onStartTrackingTouch,onStopTrackingTouch};
	
	//android.widget.CompoundButton:18
	//18. OnCheckedChangeListener
	public static final String setOnCheckedChangeListener = "void setOnCheckedChangeListener(android.widget.CompoundButton$OnCheckedChangeListener)";
	public static final String OnCheckedChangeListener = "android.widget.CompoundButton$OnCheckedChangeListener";
	public static final String onCheckedChanged = "void onCheckedChanged(android.widget.CompoundButton,boolean)";
	public static final String[] array18 = {onCheckedChanged};
	
	//android.widget.RatingBar
	//19. OnRatingBarChangeListener
	public static final String setOnRatingBarChangeListener = "void setOnRatingBarChangeListener(android.widget.RatingBar$OnRatingBarChangeListener)";
	public static final String OnRatingBarChangeListener = "android.widget.RatingBar$OnRatingBarChangeListener";
	public static final String onRatingChanged = "void onRatingChanged(android.widget.RatingBar,float,boolean)";
	public static final String[] array19 = {onRatingChanged};
	
	//android.view.ViewStub
	//20. OnInflateListener 
	public static final String setOnInflateListener = "void setOnInflateListener(android.view.ViewStub$OnInflateListener)";
	public static final String OnInflateListener = "android.view.ViewStub$OnInflateListener";
	public static final String onInflate = "void onInflate(android.view.ViewStub,android.view.View)";
	public static final String[] array20 = {onInflate};
	
	//android.widget.NumberPicker
	//21. OnScrollListener
	public static final String NumberPicker_setOnScrollListener = "void setOnScrollListener(android.widget.NumberPicker$OnScrollListener)";
	public static final String NumberPicker_OnScrollListener = "android.widget.NumberPicker$OnScrollListener";
	public static final String NumberPicker_onScrollStateChange = "void onScrollStateChange(android.widget.NumberPicker,int)";
	public static final String[] array21 = {NumberPicker_onScrollStateChange};
	
	//22. OnValueChangeListener
	public static final String setOnValueChangeListener = "void setOnValueChangeListener(android.widget.NumberPicker$OnValueChangeListener)";
	public static final String OnValueChangeListener = "android.widget.NumberPicker$OnValueChangeListener";
	public static final String onValueChange = "void onValueChange(android.widget.NumberPicker,int,int)";
	public static final String[] array22 = {ONTOUCH};
	
	//android.widget.AbsListView
	//23. OnScrollListener
	public static final String AbsListView_setOnScrollListener = "void setOnScrollListener(android.widget.AbsListView$OnScrollListener)";
	public static final String AbsListView_OnScrollListener = "android.widget.AbsListView$OnScrollListener";
	public static final String onScrollStateChanged = "void onScrollStateChanged(android.widget.AbsListView,int)";
	public static final String onScroll = "void onScroll(android.widget.AbsListView,int,int,int)";
	public static final String[] array23 = {onScrollStateChanged,onScroll};
	
	//android.content.DialogInterface
	//24. OnCancelListener
	public static final String DialogInterface_setOnCancelListener = "void setOnCancelListener(android.content.DialogInterface$OnCancelListener)";
	public static final String DialogInterface_OnCancelListener = "android.content.DialogInterface$OnCancelListener";
	public static final String DialogInterface_onCancel = "void onCancel(android.content.DialogInterface)";
	public static final String[] array24 = {DialogInterface_onCancel};
	
	//25.OnDismissListener
	public static final String DialogInterface_setOnDismissListener = "void setOnDismissListener(android.content.DialogInterface$OnDismissListener)";
	public static final String DialogInterface_OnDismissListener = "android.content.DialogInterface$OnDismissListener";
	public static final String DialogInterface_onDismiss = "void onDismiss(android.content.DialogInterface)";
	public static final String[] array25 = {DialogInterface_onDismiss};
	
	//26.OnShowListener
	public static final String DialogInterface_setOnShowListener = "void setOnShowListener(android.content.DialogInterface$OnShowListener)";
	public static final String DialogInterface_OnShowListener = "android.content.DialogInterface$OnShowListener";
	public static final String DialogInterface_onShow = "void onShow(android.content.DialogInterface)";
	public static final String[] array26 = {DialogInterface_onShow};
	
	//27.OnClickListener
	public static final String DialogInterface_setPositiveButton = "android.app.AlertDialog$Builder setPositiveButton(int,android.content.DialogInterface$OnClickListener)";
	public static final String DialogInterface_setNegativeButton = "android.app.AlertDialog$Builder setNegativeButton(int,android.content.DialogInterface$OnClickListener)";
	public static final String DialogInterface_setPositiveButton3 = "android.app.AlertDialog$Builder setPositiveButton(java.lang.CharSequence,android.content.DialogInterface$OnClickListener)";
	public static final String DialogInterface_setNegativeButton3 = "android.app.AlertDialog$Builder setNegativeButton(java.lang.CharSequence,android.content.DialogInterface$OnClickListener)";
	public static final String DialogInterface_setPositiveButtonV7 = "android.support.v7.app.AlertDialog$Builder setPositiveButton(java.lang.CharSequence,android.content.DialogInterface$OnClickListener)";
	public static final String DialogInterface_setNegativeButtonV7 = "android.support.v7.app.AlertDialog$Builder setNegativeButton(java.lang.CharSequence,android.content.DialogInterface$OnClickListener)";
	public static final String DialogInterface_setPositiveButton2 = "android.support.v7.app.AlertDialog$Builder setPositiveButton(int,android.content.DialogInterface$OnClickListener)";
	public static final String DialogInterface_setNegativeButton2 = "android.support.v7.app.AlertDialog$Builder setNegativeButton(int,android.content.DialogInterface$OnClickListener)";
	public static final String DialogInterface_setSingleChoiceItems = "android.app.AlertDialog$Builder setSingleChoiceItems(java.lang.CharSequence[],int,android.content.DialogInterface$OnClickListener)";
	public static final String DialogInterface_setOnClickListener = "void setOnClickListener(android.content.DialogInterface$OnClickListener)";
	public static final String DialogInterface_OnClickListener = "android.content.DialogInterface$OnClickListener";
	public static final String DialogInterface_onClick = "void onClick(android.content.DialogInterface,int)";
	public static final String[] array27 = {DialogInterface_onClick};
	public static final String[] DialogInterface_PositiveNegativeButton = {DialogInterface_setPositiveButton, DialogInterface_setNegativeButton,
			DialogInterface_setPositiveButtonV7,DialogInterface_setNegativeButtonV7, DialogInterface_setPositiveButton2, DialogInterface_setNegativeButton2,
			DialogInterface_setPositiveButton3, DialogInterface_setNegativeButton3};
	public static final String[] DialogInterface_SingleChoiceItems = {DialogInterface_setSingleChoiceItems};
	
	//28. OnMultiChoiceClickListener
	public static final String DialogInterface_setOnMultiChoiceClickListener = "void setOnMultiChoiceClickListener(android.content.DialogInterface$OnMultiChoiceClickListener)";
	public static final String DialogInterface_OnMultiChoiceClickListener = "android.content.DialogInterface$OnMultiChoiceClickListener";
	public static final String DialogInterface_OnMultiChoiceClickListener_onClick = "void onClick(android.content.DialogInterface,int,boolean)";
	public static final String[] array28 = {DialogInterface_OnMultiChoiceClickListener_onClick};
	
	//29.OnKeyListener
	public static final String DialogInterface_setOnKeyListener = "void setOnKeyListener(android.content.DialogInterface$OnKeyListener)";
	public static final String DialogInterface_OnKeyListener = "android.content.DialogInterface$OnKeyListener";
	public static final String DialogInterface_onKey = "boolean onKey(android.content.DialogInterface,int,android.view.KeyEvent)";
	public static final String[] array29 = {DialogInterface_onKey};
	
	//android.support.v4.view.ViewPager
	//30. OnPageChangeListener
	public static final String setOnPageChangeListener = "void setOnPageChangeListener(android.support.v4.view.ViewPager$OnPageChangeListener)";
	public static final String OnPageChangeListener = "android.support.v4.view.ViewPager$OnPageChangeListener";
	public static final String onPageSelected = "void onPageSelected(int)";
	public static final String onPageScrolled = "void onPageScrolled(int,float,int)";
	public static final String onPageScrollStateChanged = "void onPageScrollStateChanged(int)";
	public static final String[] array30 = {onPageSelected,onPageScrolled,onPageScrollStateChanged};
	
	//android.widget.SearchView
	//31. OnQueryTextListener
	public static final String setOnQueryTextListener = "void setOnQueryTextListener(android.widget.SearchView$OnQueryTextListener)";
	public static final String OnQueryTextListener = "android.widget.SearchView$OnQueryTextListener";
	public static final String onQueryTextSubmit = "boolean onQueryTextSubmit(java.lang.String)";
	public static final String onQueryTextChange = "boolean onQueryTextChange(java.lang.String)";
	public static final String[] array31 = {onQueryTextSubmit,onQueryTextChange};
	
	//32. OnCloseListener
	public static final String setOnCloseListener = "void setOnCloseListener(android.widget.SearchView$OnCloseListener)";
	public static final String OnCloseListener = "android.widget.SearchView$OnCloseListener";
	public static final String onClose = "boolean onClose()";
	public static final String[] array32 = {onClose};
	
	//33.OnSuggestionListener
	public static final String setOnSuggestionListener = "void setOnSuggestionListener(android.widget.SearchView$OnSuggestionListener)";
	public static final String OnSuggestionListener = "android.widget.SearchView$OnSuggestionListener";
	public static final String onSuggestionSelect = "boolean onSuggestionSelect(int)";
	public static final String onSuggestionClick = "boolean onSuggestionClick(int)";
	public static final String[] array33 = {onSuggestionSelect,onSuggestionClick};
	
	public static final String setOnPreferenceClickListener = "void setOnPreferenceClickListener(android.preference.Preference$OnPreferenceClickListener)";
	public static final String OnPreferenceClickListener = "android.preference.Preference$OnPreferenceClickListener";
	public static final String onPreferenceClick = "boolean onPreferenceClick(android.preference.Preference)";
	public static final String[] onPreferenceClickArray = {onPreferenceClick};
	
	//考虑一下adapter的回调
	public static final String setAdapter = "void setAdapter(android.widget.ListAdapter)";
	public static final String AlertDialog_setAdapter = "android.app.AlertDialog$Builder setAdapter(android.widget.ListAdapter,android.content.DialogInterface$OnClickListener)";
	public static final String ListAdapter = "android.widget.ListAdapter";
	public static final String getView = "android.view.View getView(int, android.view.View, android.view.ViewGroup)";
	public static final String[] getViewArray = {getView};

	//40.android.view.Menu
	public static final String onOptionsItemSelected = "boolean onOptionsItemSelected(android.view.MenuItem)";
	
	//asynchronous call back
	public static final String runOnUiThread = "void runOnUiThread(java.lang.Runnable)";
	public static final String post = "boolean post(java.lang.Runnable)";
	public static final String postDelayed = "boolean postDelayed(java.lang.Runnable,long)";
	public static final String postAtTime = "boolean postAtTime(java.lang.Runnable,long)";
	public static final String submit = "java.util.concurrent.Future submit(java.lang.Runnable)";
	public static final String RUNNABLE = "java.lang.Runnable";
	public static final String RUN = "void run()";
	public static final String[] asynRun = {RUN};
	
	static{
		
		asyncToRunnable.put(runOnUiThread, Arrays.asList(asynRun));
		asyncToRunnable.put(post, Arrays.asList(asynRun));
		asyncToRunnable.put(postDelayed, Arrays.asList(asynRun));
		asyncToRunnable.put(postAtTime, Arrays.asList(asynRun));
		asyncToRunnable.put(submit, Arrays.asList(asynRun));
		
		callBackListener.add(OnCheckedChangeListener);
		callBackListener.add(OnCloseListener);
		callBackListener.add(OnInflateListener);
		callBackListener.add(ONITEMSELECTEDLISTENER);
		callBackListener.add(OnPageChangeListener);
		callBackListener.add(OnQueryTextListener);
		callBackListener.add(OnRatingBarChangeListener);
		callBackListener.add(OnSeekBarChangeListener);
		callBackListener.add(OnSuggestionListener);
		callBackListener.add(OnValueChangeListener);
		callBackListener.add(ONCLICKLISTENER);
		callBackListener.add(ONCREATECONTEXTMENULISTENER);
		callBackListener.add(ONDRAGLISTENER);
		callBackListener.add(ONEDITORACTIONLISTENER);
		callBackListener.add(ONFOCUSCHANGELISTENER);
		callBackListener.add(ONGENERICMOTIONLISTENER);
		callBackListener.add(ONHIERARCHYCHANGELISTENER);
		callBackListener.add(ONHOVERLISTENER);
		callBackListener.add(ONITEMCLICKLISTENER);
		callBackListener.add(ONITEMLONGCLICKLISTENER);
		callBackListener.add(ONKEYLISTENER);
		callBackListener.add(ONLONGCLICKLISTENER);
		callBackListener.add(ONPREFERENCECLICKLISTENER);
		callBackListener.add(ONSYSTEMUIVISIBILITYCHANGELISTENER);
		callBackListener.add(ONTOUCHLISTENER);
		callBackListener.add(NumberPicker_OnScrollListener);
		callBackListener.add(AbsListView_OnScrollListener);
		callBackListener.add(DialogInterface_OnCancelListener);
		callBackListener.add(DialogInterface_OnClickListener);
		callBackListener.add(DialogInterface_OnDismissListener);
		callBackListener.add(DialogInterface_OnKeyListener);
		callBackListener.add(DialogInterface_OnMultiChoiceClickListener);
		callBackListener.add(DialogInterface_OnShowListener);
		callBackListener.add(OnPreferenceClickListener);
		callBackListener.add(ListAdapter);
		
		viewToCallBacks.put(setOnCheckedChangeListener,Arrays.asList(array18));
		viewToCallBacks.put(setOnCloseListener,Arrays.asList(array32));
		viewToCallBacks.put(setOnInflateListener,Arrays.asList(array20));
		viewToCallBacks.put(SETONITEMSELECTEDLISTENER,Arrays.asList(array16));
		viewToCallBacks.put(setOnPageChangeListener,Arrays.asList(array30));
		viewToCallBacks.put(setOnQueryTextListener,Arrays.asList(array31));
		viewToCallBacks.put(setOnRatingBarChangeListener,Arrays.asList(array19));
		viewToCallBacks.put(setOnSeekBarChangeListener,Arrays.asList(array17));
		viewToCallBacks.put(setOnSuggestionListener,Arrays.asList(array33));
		viewToCallBacks.put(setOnValueChangeListener,Arrays.asList(array22));
		viewToCallBacks.put(SETONCLICKLISTENER,Arrays.asList(array8));
		viewToCallBacks.put(SETONCREATECONTEXTMENULISTENER,Arrays.asList(array9));
		viewToCallBacks.put(SETONDRAGLISTENER,Arrays.asList(array6));
		viewToCallBacks.put(SETONEDITORACTIONLISTENER,Arrays.asList(array12));
		viewToCallBacks.put(SETONFOCUSCHANGELISTENER,Arrays.asList(array7));
		viewToCallBacks.put(SETONGENERICMOTIONLISTENER,Arrays.asList(array4));
		viewToCallBacks.put(SETONHIERARCHYCHANGELISTENER,Arrays.asList(array13));
		viewToCallBacks.put(SETONHOVERLISTENER,Arrays.asList(array3));
		viewToCallBacks.put(SETONITEMCLICKLISTENER,Arrays.asList(array14));
		viewToCallBacks.put(SETONITEMLONGCLICKLISTENER,Arrays.asList(array15));
		viewToCallBacks.put(SETONKEYLISTENER,Arrays.asList(array1));
		viewToCallBacks.put(SETONLONGCLICKLISTENER,Arrays.asList(array5));
		viewToCallBacks.put(SETONPREFERENCECLICKLISTENER,Arrays.asList(array11));
		viewToCallBacks.put(SETONSYSTEMUIVISIBILITYCHANGELISTENER,Arrays.asList(array10));
		viewToCallBacks.put(SETONTOUCHLISTENER,Arrays.asList(array2));
		viewToCallBacks.put(NumberPicker_setOnScrollListener,Arrays.asList(array21));
		viewToCallBacks.put(AbsListView_setOnScrollListener,Arrays.asList(array23));
		viewToCallBacks.put(setOnPreferenceClickListener, Arrays.asList(onPreferenceClickArray));
		viewToCallBacks.put(setAdapter, Arrays.asList(getViewArray));
		viewToCallBacks.put(AlertDialog_setAdapter, Arrays.asList(getViewArray));
		
		dialogToCallBacks.put(DialogInterface_setOnCancelListener,Arrays.asList(array24));
		dialogToCallBacks.put(DialogInterface_setOnClickListener,Arrays.asList(array27));
		dialogToCallBacks.put(DialogInterface_setPositiveButton,Arrays.asList(array27));
		dialogToCallBacks.put(DialogInterface_setNegativeButton,Arrays.asList(array27));
		dialogToCallBacks.put(DialogInterface_setPositiveButtonV7,Arrays.asList(array27));
		dialogToCallBacks.put(DialogInterface_setNegativeButtonV7,Arrays.asList(array27));
		dialogToCallBacks.put(DialogInterface_setPositiveButton2,Arrays.asList(array27));
		dialogToCallBacks.put(DialogInterface_setNegativeButton2,Arrays.asList(array27));
		dialogToCallBacks.put(DialogInterface_setPositiveButton3,Arrays.asList(array27));
		dialogToCallBacks.put(DialogInterface_setNegativeButton3,Arrays.asList(array27));
		dialogToCallBacks.put(DialogInterface_setSingleChoiceItems,Arrays.asList(array27));
		dialogToCallBacks.put(DialogInterface_setOnDismissListener,Arrays.asList(array25));
		dialogToCallBacks.put(DialogInterface_setOnKeyListener,Arrays.asList(array29));
		dialogToCallBacks.put(DialogInterface_setOnMultiChoiceClickListener,Arrays.asList(array28));
		dialogToCallBacks.put(DialogInterface_setOnShowListener,Arrays.asList(array26));
		
	}
	
	public static Set<String> getDialogRegistars() {
		return dialogToCallBacks.keySet();
	}

	public static Set<String> getViewRegistars() {
		return viewToCallBacks.keySet();
	}
	
	public static Set<String> getAsyncs(){
		return asyncToRunnable.keySet();
	}
	
	public static List<String> getCallBackByRegister(String regis) {
		if(getViewRegistars().contains(regis))
			return viewToCallBacks.get(regis);
		else if(getDialogRegistars().contains(regis))
			return dialogToCallBacks.get(regis);
		else 
			return null;
	}
	
	public static List<String> getRunnableByAsync(String async){
		if(getAsyncs().contains(async))
			return asyncToRunnable.get(async);
		else
			return null;
	}

}
