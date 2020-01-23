package com.bayer.web.core.helpers;

import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.DamConstants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.resource.ValueMap;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.bayer.web.core.helpers.C7ClassHelper;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.ArrayList;

public class C7ClassHelper {

	private static final Logger logger = LoggerFactory.getLogger(C7ClassHelper.class);

	static public class AssetData {
		public String fileType;
		public String filePath;
		public String fileName;
		public String fileTitle;
		public String tags;

		public AssetData(Resource resource, String tag) {
			Asset asset = resource.adaptTo(Asset.class);
			if (asset != null) {
				this.fileType = asset.getMimeType();
				this.filePath = asset.getPath();
				this.fileName = asset.getName();
				this.fileTitle = asset.getMetadataValue("dc:title");
				this.tags = asset.getMetadataValue("cq:tags");
			}
		}

		public JsonObject getAsJson() {
			JsonObject assetObject = new JsonObject();
			assetObject.addProperty("fileType", this.fileType);
			assetObject.addProperty("filePath", this.filePath);
			assetObject.addProperty("fileName", this.fileName);
			assetObject.addProperty("fileTitle", this.fileTitle);
			return assetObject;
		}

		public String getFileType() {
			return this.fileType;
		}

		public void setFileType(String fileType) {
			this.fileType = fileType;
		}

		public String getFilePath() {
			return this.filePath;
		}

		public void setFilePath(String filePath) {
			this.filePath = filePath;
		}

		public String getFileName() {
			return this.fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getFileTitle() {
			return this.fileTitle;
		}

		public void setFileTitle(String fileTitle) {
			this.fileTitle = fileTitle;
		}

		public String getTags() {
			return this.tags;
		}

		public void setTags(String tags) {
			this.tags = tags;
		}
	}

	static public class Folder {
		public ArrayList<AssetData> assets;
		public ArrayList<Folder> subfolders;
		public String title;
		public String name;

		public Folder(Resource resource, String tag) {
			this.assets = new ArrayList<AssetData>();
			this.subfolders = new ArrayList<Folder>();

			// Set name and title
			this.name = resource.getName();
			Resource content = resource.getChild(JcrConstants.JCR_CONTENT);
			if (content != null) {
				ValueMap valueMap = content.getValueMap();
				if (valueMap != null && valueMap.containsKey(JcrConstants.JCR_TITLE)) {
					this.title = valueMap.get(JcrConstants.JCR_TITLE, String.class);
				}
			}

			// Loop through all children of the folder
			Iterable<Resource> children = resource.getChildren();
			for (Resource child : children) {
				// If it's a folder, get the folder assets
				if (child.getResourceType().equals("sling:Folder")
						|| child.getResourceType().equals("sling:OrderedFolder")) {
					Folder childAssetFolder = new Folder(child, tag);
					this.subfolders.add(childAssetFolder);
					// If it's an asset, get the asset data
				} else if (child.getResourceType().equals("dam:Asset")) {
					ContentFragment contentFragment = child.adaptTo(ContentFragment.class);
					if (contentFragment != null) {
						FAQ faq = new FAQ(child, tag);
						if (faq.getFileName() != null) {
							if (tag != null && faq.getTags() != null && faq.getTags().contains(tag)) {
								this.assets.add(faq);
							} else if (tag == null) {
								this.assets.add(faq);
							}
						}
					} else {
						AssetData assetData = new AssetData(child, tag);
						if (assetData.getFileName() != null) {
							if (tag != null && assetData.getTags() != null && assetData.getTags().contains(tag)) {
								this.assets.add(assetData);
							} else if (tag == null) {
								this.assets.add(assetData);
							}
						}
					}
				}
			}
		};

		public JsonObject getAsJson() {
			JsonObject folderObject = new JsonObject();
			JsonArray jsonAssetArray = new JsonArray();
			JsonArray jsonFolderArray = new JsonArray();
			for (AssetData asset : this.assets) {
				jsonAssetArray.add(asset.getAsJson());
			}
			for (Folder subfolder : this.subfolders) {
				jsonFolderArray.add(subfolder.getAsJson());
			}
			folderObject.addProperty("title", this.title);
			folderObject.addProperty("name", this.name);
			folderObject.add("assets", jsonAssetArray);
			folderObject.add("subfolders", jsonFolderArray);
			return folderObject;
		}

		public ArrayList<AssetData> getAssets() {
			return this.assets;
		}

		public void setAssets(ArrayList<AssetData> assets) {
			this.assets = assets;
		}

		public ArrayList<Folder> getSubfolders() {
			return this.subfolders;
		}

		public void setSubfolders(ArrayList<Folder> subfolders) {
			this.subfolders = subfolders;
		}

		public String getTitle() {
			return this.title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	static public class FAQ extends AssetData {

		public String title;
		public String question;
		public String answer;
		public String[] tags;

		public FAQ(Resource resource, String tag) {
			super(resource, tag);
			ContentFragment contentFragment = resource.adaptTo(ContentFragment.class);
			if (contentFragment != null) {
				Iterator<ContentElement> elements = contentFragment.getElements();

				this.title = contentFragment.getTitle();

				for (Iterator iter = elements; iter.hasNext();) {
					ContentElement element = (ContentElement) iter.next();
					if (element.getName().equals("tags")) {
						String[] tags = element.getContent().toString().split("\n");
						this.tags = tags;
					} else if (element.getName().equals("question")) {
						this.question = element.getContent().toString();
					} else if (element.getName().equals("answer")) {
						this.answer = element.getContent().toString();
					}
				}
			}
		}

		public JsonObject getAsJson() {
			JsonObject assetObject = new JsonObject();
			assetObject.addProperty("title", this.title);
			assetObject.addProperty("question", this.question);
			assetObject.addProperty("answer", this.answer);
			JsonArray tagsArray = new JsonArray();
			if (this.tags != null) {
				for (String tagItem : this.tags) {
					tagsArray.add(tagItem);
					assetObject.add("tags", tagsArray);
				}
			}
			return assetObject;
		}

		public String getTitle() {
			return this.title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getQuestion() {
			return this.question;
		}

		public void setQuestion(String question) {
			this.question = question;
		}

		public String getAnswer() {
			return this.answer;
		}

		public void setAnswer(String answer) {
			this.answer = answer;
		}

	}
}
