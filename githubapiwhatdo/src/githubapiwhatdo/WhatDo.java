package githubapiwhatdo;

import java.io.IOException;
import java.util.List;

import org.kohsuke.github.*;
import org.kohsuke.github.GHCommit.File;

public class WhatDo {
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
		}
		
	}
}
