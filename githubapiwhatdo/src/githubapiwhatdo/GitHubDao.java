package githubapiwhatdo;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.client.*;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.kohsuke.github.*;
import org.kohsuke.github.GHCommit.File;

public class WhatDo {
	/*
	public static void main(String args[]) throws IOException {
		System.out.println("What do?");
		GitHub github = GitHub.connect("pammil", "f2b7e61733b957d2827ee06d6f754e9f38d2423e");
		GHRepository teamTwo = github.getRepository("iambchan/Team-02");
		List<GHCommit> commits = teamTwo.listCommits().asList();
		System.out.println(teamTwo.getName());
		for(GHCommit c : commits) {
			for(File f : c.getFiles()) {
				System.out.println(f.getFileName());
			}
		}*/
	
	public static void main(String args[]) throws IOException {
		GitHubClient client = new GitHubClient();
		client.setOAuth2Token("f2b7e61733b957d2827ee06d6f754e9f38d2423e");
		RepositoryService repoService = new RepositoryService();
		Repository teamTwo = repoService.getRepository("iambchan", "Team-02");
		CommitService commitService = new CommitService();
		for(PageIterator<RepositoryCommit> commits = commitService.pageCommits(teamTwo); commits.hasNext();) {
			for(RepositoryCommit commit : commits.next()) {
				System.out.println(commit.getUrl());
				List<String> javaFileNames = ParseCommit.getJavaFileNames(commit);
				for(String s : javaFileNames) {
					System.out.println(s);
				}
				System.out.println();
			}
		}
	
	}
	//git file statuses: added, removed, modified
}
