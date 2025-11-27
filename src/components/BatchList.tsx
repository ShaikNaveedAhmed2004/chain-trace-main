import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Eye, Trash2, Edit } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { type Batch, type Product } from "@/lib/api";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { useState } from "react";

const statusColors = {
  CREATED: "bg-secondary text-secondary-foreground",
  IN_TRANSIT: "bg-warning text-warning-foreground",
  DELIVERED: "bg-success text-success-foreground",
  SOLD: "bg-primary text-primary-foreground",
  MANUFACTURED: "bg-info text-info-foreground",
};

interface BatchListProps {
  batches: Batch[];
  products?: Product[];
  onRefresh?: () => void;
  onDelete?: (id: number) => void;
  onEdit?: (batch: Batch) => void;
}

const BatchList = ({ batches, products = [], onRefresh, onDelete, onEdit }: BatchListProps) => {
  const navigate = useNavigate();
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const getProductName = (productId: number) => {
    const product = products.find(p => p.id === productId);
    return product ? product.name : `Product #${productId}`;
  };

  if (batches.length === 0) {
    return (
      <div className="text-center py-8 text-muted-foreground">
        No batches found. Create your first batch to get started.
      </div>
    );
  }

  return (
    <>
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Batch Number</TableHead>
              <TableHead>Product</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Location</TableHead>
              <TableHead>Quantity</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {batches.map((batch) => (
              <TableRow key={batch.id}>
                <TableCell className="font-mono text-sm">{batch.batchNumber}</TableCell>
                <TableCell>{getProductName(batch.productId)}</TableCell>
                <TableCell>
                  <Badge className={statusColors[batch.status as keyof typeof statusColors] || "bg-secondary"}>
                    {batch.status.replace("_", " ")}
                  </Badge>
                </TableCell>
                <TableCell>{batch.currentLocation}</TableCell>
                <TableCell>{batch.quantity}</TableCell>
                <TableCell className="text-right">
                  <div className="flex justify-end gap-2">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => navigate(`/track/${batch.id}`)}
                    >
                      <Eye className="w-4 h-4 mr-1" />
                      Track
                    </Button>
                    {onEdit && (
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => onEdit(batch)}
                      >
                        <Edit className="w-4 h-4 mr-1" />
                        Edit
                      </Button>
                    )}
                    {onDelete && (
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => setDeleteId(batch.id)}
                      >
                        <Trash2 className="w-4 h-4 mr-1 text-destructive" />
                        Delete
                      </Button>
                    )}
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {onDelete && (
        <AlertDialog open={deleteId !== null} onOpenChange={(open) => !open && setDeleteId(null)}>
          <AlertDialogContent>
            <AlertDialogHeader>
              <AlertDialogTitle>Are you sure?</AlertDialogTitle>
              <AlertDialogDescription>
                This action cannot be undone. This will permanently delete the batch.
              </AlertDialogDescription>
            </AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel>Cancel</AlertDialogCancel>
              <AlertDialogAction
                onClick={() => {
                  if (deleteId) onDelete(deleteId);
                  setDeleteId(null);
                }}
              >
                Delete
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      )}
    </>
  );
};

export default BatchList;
