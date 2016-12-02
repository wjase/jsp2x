<%@ include file="taglibs.jspf" %>

<%@ tag 
	language="java"
	pageEncoding="UTF-8"
	body-content="empty"
	description="Renders a script tag with some necessary utility functions in JavaScript." %>
	
<script type="text/javascript">


/** 
 * Returns the number of selected rows in the pager. The returned value includes rows selected on 
 * other pages of the pager, not just the current one. The caller needs to provide the total number
 * of rows in the pager. The page context also needs to contain a form object having a 
 * getSelection() method that returns an object exposing the Java collection interface. For example, 
 * any form that is a subclass of PagingForm meets that requirement. 
 */  

function Pager_countSelectedRows( totalRows ) {
	var selectAllCheckbox = $('selectAll');
	if( ! selectAllCheckbox ) return 0;
	var count, selectAll = selectAllCheckbox.checked, discount = false;
	if( selectAll == ${form.selectAll} ) {
		count = ${form.totalSelected - fn:length( form.selection )};
	} else {
		if( selectAll ) {
			count = totalRows
			discount = true;
		} else {
			count = 0
		}
	}
	$('form').getElementsByClassName('selection').each( function( checkbox )  {
		if( checkbox.type && checkbox.type == 'checkbox' ) {
			if( discount ) {
				if( ! checkbox.checked ) count--;
			} else {
				if( checkbox.checked ) count++;
			}		
		}
	});
	return count;
}

/** 
 * Updates the selection count header (the two numbers in the pager header separated by a slash) 
 * and returns the number of selected rows in the pager.
 */ 

function Pager_selectionChanged( totalRows ) {
	var selectedRows = Pager_countSelectedRows( totalRows );	
	$('selectedRows').update( '' + selectedRows + '/' );
	$('totalRows').update( '' + totalRows );
	return selectedRows;
}

function Pager_selectAll( checked ) {
	$('form').getElementsByClassName('selection').each( function( checkbox )  {
		if( checkbox.type && checkbox.type == 'checkbox' && ! checkbox.disabled ) {
			checkbox.checked = checked;
		}
	} );
}

</script>