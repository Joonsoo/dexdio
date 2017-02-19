package com.giyeok.dexdio.views.classdetailview;

import java.util.ArrayList;

import org.eclipse.draw2d.ColorConstants;

import com.giyeok.dexdio.model0.DexClass;
import com.giyeok.dexdio.model0.DexCodeItem;
import com.giyeok.dexdio.model0.DexMethod;
import com.giyeok.dexdio.views.classdetailview.ClassContentView.MethodContentViewStyle;
import com.giyeok.dexdio.views.classdetailview.listings.InstSemListing;
import com.giyeok.dexdio.views.classdetailview.listings.MethodContentListing;
import com.giyeok.dexdio.views.classdetailview.listings.RawInstructionListing;
import com.giyeok.dexdio.views.classdetailview.listings.ReplacedAliveInstSemListing;
import com.giyeok.dexdio.views.classdetailview.listings.ReplacedInstSemListing;
import com.giyeok.dexdio.views.classdetailview.listings.StructuredReplacedInstSemListing;
import com.giyeok.dexdio.widgets.GroupedLabelListWidget.ItemGroup;
import com.giyeok.dexdio.widgets.GroupedLabelListWidget.ItemGroupSelectionListener;
import com.giyeok.dexdio.widgets.Label;

class MethodContentView {
	
	public MethodContentView(final DexClass showing, final DexMethod method, ArrayList<Label> items, ClassContentView cdv, MethodContentViewStyle viewStyle, final DexClassDetailViewer controller) {
		DexCodeItem codeitem = method.getCodeItem();

		MethodContentListing listing;
		switch (viewStyle) {
		case INSTRUCTIONS:
			listing = new RawInstructionListing(showing, method, codeitem, items, cdv, viewStyle, controller);
			break;
		case INSTSEM_RAW:
			listing = new InstSemListing(showing, method, codeitem, items, cdv, viewStyle, controller);
			break;
		case INSTSEM_REPLACED:
			listing = new ReplacedInstSemListing(showing, method, codeitem, items, cdv, viewStyle, controller);
			break;
		case INSTSEM_REPLACED_ALIVES:
			listing = new ReplacedAliveInstSemListing(showing, method, codeitem, items, cdv, viewStyle, controller);
			break;
		case STRUCTURED_INSTSEM_REPLACED_ALIVES:
			listing = new StructuredReplacedInstSemListing(showing, method, codeitem, items, cdv, viewStyle, controller);
			break;
		default:
			// unknown view style
			return;
		}
		
		int methodTitleTop = items.size();

		listing.addTitle();
		
		int methodTitleEnd = items.size();
		
		// method instructions
		
		if (codeitem != null) {
			if (method.isIntanceMethod()) {
				items.add(Label.newLabel(new Label[] {
						Label.newLabel("            // with ", ColorConstants.darkBlue), listing.getInstanceRegisterLabel() }));
			}
			items.add(Label.newLabel("        // registers: " + codeitem.getRegistersSize(), ColorConstants.darkBlue));
			items.add(Label.newLabel("        // ins: " + codeitem.getInsSize(), ColorConstants.darkBlue));
			items.add(Label.newLabel("        // outs: " + codeitem.getOutsSize(), ColorConstants.darkBlue));
			
			listing.addContent();
			
			items.add(Label.newLabel("    }"));
		}
		
		ItemGroup ig = cdv.new ItemGroup(methodTitleTop, items.size(), methodTitleTop + 1, methodTitleEnd + 1, new ItemGroupSelectionListener() {
			
			@Override
			public void selected() {
				controller.showMethod(method);
			}

			@Override
			public void deselected() {
				controller.showClass(showing);
			}

			@Override
			public void deselectedToMove() {
				// nothing to do
			}
		});
		cdv.addMethodsToGroup(method, ig);
		cdv.addItemGroup(ig);

		items.add(Label.newEmptyLabel());
	}
}
